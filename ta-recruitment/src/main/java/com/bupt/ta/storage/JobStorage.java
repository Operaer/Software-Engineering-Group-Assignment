package com.bupt.ta.storage;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.Job;
import javax.servlet.ServletContext;
import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Text file-backed storage for jobs.
 */
public class JobStorage {
    private static final String STORAGE_PATH = AppConfig.JOBS_FILE;
    private final File storageFile;
    private final javax.servlet.ServletContext servletContext;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Constructs a {@code JobStorage} instance and ensures the underlying text
     * storage file exists.
     *
     * @param context the ServletContext used to resolve the real path to the storage file
     */
    public JobStorage(ServletContext context) {
        this.storageFile = new File(context.getRealPath(STORAGE_PATH));
        this.servletContext = context;
        ensureStorageExists();
    }

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
            throw new IllegalStateException("Unable to initialize job storage", e);
        }
    }

    /**
     * Loads all job records from the pipe-delimited text storage file. Each line
     * is parsed into a {@link Job}; lines with fewer than the expected number of
     * fields or with unparseable dates are silently skipped.
     *
     * @return a mutable list of all stored jobs (never {@code null})
     * @throws IllegalStateException if the storage file cannot be read
     */
    private List<Job> loadAll() {
        List<Job> jobs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 8) {
                    LocalDate deadline = parseDeadline(parts[5]);
                    Instant postedAt = parseInstant(parts[7]);
                    if (deadline == null || postedAt == null) {
                        continue;
                    }
                    Job job = new Job();
                    job.setId(parts[0]);
                    job.setTitle(parts[1]);
                    job.setModuleCode(parts[2]);
                    job.setWorkload(parts[3]);
                    job.setRequirements(parts[4]);
                    job.setDeadline(deadline);
                    job.setPostedBy(parts[6]);
                    job.setPostedAt(postedAt);
                    job.setStatus(parts.length >= 9 ? parts[8] : Job.STATUS_OPEN);
                    if (parts.length >= 10) {
                        Instant updatedAt = parseInstant(parts[9]);
                        job.setUpdatedAt(updatedAt != null ? updatedAt : job.getPostedAt());
                    } else {
                        job.setUpdatedAt(job.getPostedAt());
                    }
                    jobs.add(job);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read jobs storage", e);
        }
        return jobs;
    }

    /**
     * Parses a persisted deadline while tolerating malformed stored records.
     *
     * @param rawDeadline raw deadline text from storage
     * @return parsed deadline, or {@code null} when the value is missing or invalid
     */
    private LocalDate parseDeadline(String rawDeadline) {
        if (rawDeadline == null || rawDeadline.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(rawDeadline, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a persisted timestamp while tolerating malformed stored records.
     *
     * @param rawInstant raw timestamp text from storage
     * @return parsed timestamp, or {@code null} when the value is missing or invalid
     */
    private Instant parseInstant(String rawInstant) {
        if (rawInstant == null || rawInstant.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(rawInstant);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Persists all jobs to the pipe-delimited text storage file, overwriting any
     * existing content.
     *
     * @param jobs the list of jobs to write (must not be {@code null})
     * @throws IllegalStateException if the file cannot be written
     */
    private void saveAll(List<Job> jobs) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile))) {
            for (Job job : jobs) {
                writer.write(job.getId() + "|" +
                           job.getTitle() + "|" +
                           job.getModuleCode() + "|" +
                           job.getWorkload() + "|" +
                           job.getRequirements() + "|" +
                           job.getDeadline().format(DATE_FORMATTER) + "|" +
                           job.getPostedBy() + "|" +
                           job.getPostedAt().toString() + "|" +
                           (job.getStatus() == null ? Job.STATUS_OPEN : job.getStatus()) + "|" +
                           (job.getUpdatedAt() == null ? job.getPostedAt().toString() : job.getUpdatedAt().toString()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write jobs storage", e);
        }
    }

    /**
     * Saves a new job record to persistent storage and records an audit-log entry
     * for the creation.
     *
     * @param job the job to persist (must not be {@code null})
     */
    public void save(Job job) {
        List<Job> jobs = loadAll();
        jobs.add(job);
        saveAll(jobs);
        try {
            com.bupt.ta.storage.AuditLogStorage audit = new com.bupt.ta.storage.AuditLogStorage(servletContext);
            audit.add(new com.bupt.ta.model.AuditLogEntry(job.getPostedBy(), "Position Create", job.getId(), "Created position: " + job.getTitle()));
        } catch (Exception ignored) {}
    }

    /**
     * Updates an existing job record in storage. If the job ID is not found,
     * the job is added as a new record. An audit-log entry is recorded on a
     * best-effort basis.
     *
     * @param updatedJob the job with updated fields (must not be {@code null})
     */
    public void update(Job updatedJob) {
        List<Job> jobs = loadAll();
        boolean replaced = false;
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getId().equals(updatedJob.getId())) {
                jobs.set(i, updatedJob);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            jobs.add(updatedJob);
        }
        saveAll(jobs);
        try {
            com.bupt.ta.storage.AuditLogStorage audit = new com.bupt.ta.storage.AuditLogStorage(servletContext);
            audit.add(new com.bupt.ta.model.AuditLogEntry(updatedJob.getPostedBy(), "Position Update", updatedJob.getId(), "Updated position: " + updatedJob.getTitle()));
        } catch (Exception ignored) {}
    }

    /**
     * Marks a job as archived by setting its status to {@link Job#STATUS_ARCHIVED}
     * and updating its {@code updatedAt} timestamp. An audit-log entry is recorded
     * on a best-effort basis.
     *
     * @param id the identifier of the job to archive
     */
    public void archive(String id) {
        List<Job> jobs = loadAll();
        for (Job job : jobs) {
            if (job.getId().equals(id)) {
                job.setStatus(Job.STATUS_ARCHIVED);
                job.setUpdatedAt(Instant.now());
                break;
            }
        }
        saveAll(jobs);
        try {
            // best-effort: find job to log operator unknown here
            com.bupt.ta.storage.AuditLogStorage audit = new com.bupt.ta.storage.AuditLogStorage(servletContext);
            audit.add(new com.bupt.ta.model.AuditLogEntry("system", "Position Archive", id, "Archived position id: " + id));
        } catch (Exception ignored) {}
    }

    /**
     * Creates a new job record with the given parameters, persists it, and returns
     * the fully populated {@link Job} object.
     *
     * @param title        the job title
     * @param moduleCode   the associated module or course code
     * @param workload     a description of the expected workload
     * @param requirements a description of the position requirements
     * @param deadline     the application deadline
     * @param postedBy     the email or identifier of the user posting the job
     * @return the newly created and persisted job
     */
    public Job createNew(String title, String moduleCode, String workload, String requirements, LocalDate deadline, String postedBy) {
        Job job = new Job();
        job.setId(UUID.randomUUID().toString());
        job.setTitle(title);
        job.setModuleCode(moduleCode);
        job.setWorkload(workload);
        job.setRequirements(requirements);
        job.setDeadline(deadline);
        job.setPostedBy(postedBy);
        job.setPostedAt(Instant.now());
        job.setUpdatedAt(job.getPostedAt());
        job.setStatus(Job.STATUS_OPEN);
        save(job);
        return job;
    }

    /**
     * Returns every job record currently stored.
     *
     * @return a list of all jobs (never {@code null})
     */
    public List<Job> findAll() {
        return loadAll();
    }

    /**
     * Finds a single job by its unique identifier.
     *
     * @param id the job identifier to search for
     * @return the matching job, or {@code null} if not found
     */
    public Job findById(String id) {
        return loadAll().stream()
                .filter(job -> job.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
