package com.bupt.ta.storage;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.JobHistoryEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Text-backed storage for job modification history.
 */
public class JobHistoryStorage {
    private static final String STORAGE_PATH = AppConfig.JOB_HISTORY_FILE;
    private final File storageFile;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Constructs a {@code JobHistoryStorage} instance and ensures the underlying
     * text-based storage file exists.
     *
     * @param context the ServletContext used to resolve the real path to the storage file
     */
    public JobHistoryStorage(ServletContext context) {
        this.storageFile = new File(context.getRealPath(STORAGE_PATH));
        this.context = context;
        ensureStorageExists();
    }

    private final ServletContext context;

    /**
     * Ensures that the directory and storage file exist, creating them if necessary.
     *
     * @throws IllegalStateException if the directory or file cannot be created
     */
    private void ensureStorageExists() {
        try {
            File parent = storageFile.getParentFile();
            if (!parent.exists()) {
                Files.createDirectories(parent.toPath());
            }
            if (!storageFile.exists()) {
                storageFile.createNewFile();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize job history storage", e);
        }
    }

    /**
     * Loads all job history entries from the pipe-delimited text storage file.
     * Each line is parsed into a {@link JobHistoryEntry}; malformed lines are
     * silently skipped.
     *
     * @return a mutable list of all stored entries (never {@code null})
     * @throws IllegalStateException if the storage file cannot be read
     */
    private List<JobHistoryEntry> loadAll() {
        List<JobHistoryEntry> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 6);
                if (parts.length >= 5) {
                    JobHistoryEntry entry = new JobHistoryEntry();
                    entry.setJobId(parts[0]);
                    entry.setChangedAt(Instant.parse(parts[1]));
                    entry.setChangedBy(parts[2]);
                    entry.setAction(parts[3]);
                    entry.setDetails(URLDecoder.decode(parts[4], StandardCharsets.UTF_8));
                    if (parts.length == 6) {
                        entry.setSnapshot(URLDecoder.decode(parts[5], StandardCharsets.UTF_8));
                    }
                    entries.add(entry);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read job history storage", e);
        }
        return entries;
    }

    /**
     * Records a job history entry without a full-job snapshot. Delegates to
     * {@link #record(String, String, String, Job, String)} with a {@code null}
     * snapshot.
     *
     * @param jobId     the job identifier
     * @param action    a short description of the action performed
     * @param details   additional details about the change
     * @param changedBy the user who performed the change
     */
    public void record(String jobId, String action, String details, String changedBy) {
        record(jobId, action, details, null, changedBy);
    }

    /**
     * Records a job history entry with an optional full-job JSON snapshot.
     * The entry is appended to the text storage file and an audit-log entry is
     * also created on a best-effort basis.
     *
     * @param jobId     the job identifier
     * @param action    a short description of the action performed
     * @param details   additional details about the change
     * @param snapshot  a full snapshot of the job at the time of the change
     *                  (may be {@code null} to omit)
     * @param changedBy the user who performed the change
     * @throws IllegalStateException if the storage file cannot be written
     */
    public void record(String jobId, String action, String details, Job snapshot, String changedBy) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile, true))) {
            String encodedDetails = URLEncoder.encode(details == null ? "" : details, StandardCharsets.UTF_8);
            String encodedSnapshot = snapshot != null ? URLEncoder.encode(mapper.writeValueAsString(snapshot), StandardCharsets.UTF_8) : "";
            writer.write(jobId + "|" + Instant.now().toString() + "|" + changedBy + "|" + action + "|" + encodedDetails + "|" + encodedSnapshot);
            writer.newLine();
            try {
                com.bupt.ta.storage.AuditLogStorage audit = new com.bupt.ta.storage.AuditLogStorage(context);
                audit.add(new com.bupt.ta.model.AuditLogEntry(changedBy, "Job History", jobId, action + ": " + details));
            } catch (Exception ignored) {}
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write job history storage", e);
        }
    }

    /**
     * Finds the most recent job snapshot for the given job ID by scanning the
     * history entries in reverse chronological order.
     *
     * @param jobId the job identifier to search for
     * @return the most recent {@link Job} snapshot, or {@code null} if none exists
     * @throws IllegalStateException if a stored snapshot cannot be deserialised
     */
    public Job findLastSnapshot(String jobId) {
        List<JobHistoryEntry> entries = loadAll();
        for (int i = entries.size() - 1; i >= 0; i--) {
            JobHistoryEntry entry = entries.get(i);
            if (jobId != null && jobId.equals(entry.getJobId()) && entry.getSnapshot() != null && !entry.getSnapshot().isBlank()) {
                try {
                    return mapper.readValue(entry.getSnapshot(), Job.class);
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to deserialize job snapshot", e);
                }
            }
        }
        return null;
    }

    /**
     * Finds all history entries associated with the given job ID.
     *
     * @param jobId the job identifier to search for
     * @return a list of matching entries (never {@code null})
     */
    public List<JobHistoryEntry> findByJobId(String jobId) {
        return loadAll().stream()
                .filter(entry -> jobId != null && jobId.equals(entry.getJobId()))
                .collect(Collectors.toList());
    }

    /**
     * Finds a single history entry matching both the job ID and the exact change
     * timestamp.
     *
     * @param jobId    the job identifier to search for
     * @param changedAt the exact change timestamp to match
     * @return the matching entry, or {@code null} if no match is found or either
     *         parameter is {@code null}
     */
    public JobHistoryEntry findByJobIdAndChangedAt(String jobId, Instant changedAt) {
        if (jobId == null || changedAt == null) {
            return null;
        }
        return loadAll().stream()
                .filter(entry -> jobId.equals(entry.getJobId()) && changedAt.equals(entry.getChangedAt()))
                .findFirst()
                .orElse(null);
    }
}
