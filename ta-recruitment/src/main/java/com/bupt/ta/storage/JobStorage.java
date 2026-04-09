package com.bupt.ta.storage;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.Job;
import javax.servlet.ServletContext;
import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Text file-backed storage for jobs.
 */
public class JobStorage {
    private static final String STORAGE_PATH = AppConfig.JOBS_FILE;
    private final File storageFile;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public JobStorage(ServletContext context) {
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
            throw new IllegalStateException("Unable to initialize job storage", e);
        }
    }

    private List<Job> loadAll() {
        List<Job> jobs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 8) {
                    Job job = new Job();
                    job.setId(parts[0]);
                    job.setTitle(parts[1]);
                    job.setModuleCode(parts[2]);
                    job.setWorkload(parts[3]);
                    job.setRequirements(parts[4]);
                    job.setDeadline(LocalDate.parse(parts[5], DATE_FORMATTER));
                    job.setPostedBy(parts[6]);
                    job.setPostedAt(Instant.parse(parts[7]));
                    job.setStatus(parts.length >= 9 ? parts[8] : Job.STATUS_OPEN);
                    if (parts.length >= 10) {
                        job.setUpdatedAt(Instant.parse(parts[9]));
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

    public void save(Job job) {
        List<Job> jobs = loadAll();
        jobs.add(job);
        saveAll(jobs);
    }

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
    }

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
    }

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

    public List<Job> findAll() {
        return loadAll();
    }

    public Job findById(String id) {
        return loadAll().stream()
                .filter(job -> job.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}