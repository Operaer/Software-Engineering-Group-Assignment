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

    public JobHistoryStorage(ServletContext context) {
        this.storageFile = new File(context.getRealPath(STORAGE_PATH));
        ensureStorageExists();
    }

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

    public void record(String jobId, String action, String details, String changedBy) {
        record(jobId, action, details, null, changedBy);
    }

    public void record(String jobId, String action, String details, Job snapshot, String changedBy) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile, true))) {
            String encodedDetails = URLEncoder.encode(details == null ? "" : details, StandardCharsets.UTF_8);
            String encodedSnapshot = snapshot != null ? URLEncoder.encode(mapper.writeValueAsString(snapshot), StandardCharsets.UTF_8) : "";
            writer.write(jobId + "|" + Instant.now().toString() + "|" + changedBy + "|" + action + "|" + encodedDetails + "|" + encodedSnapshot);
            writer.newLine();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write job history storage", e);
        }
    }

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

    public List<JobHistoryEntry> findByJobId(String jobId) {
        return loadAll().stream()
                .filter(entry -> jobId != null && jobId.equals(entry.getJobId()))
                .collect(Collectors.toList());
    }
}
