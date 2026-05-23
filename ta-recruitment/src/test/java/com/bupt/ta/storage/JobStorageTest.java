package com.bupt.ta.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bupt.ta.TestUtils;
import com.bupt.ta.model.Job;

/**
 * Unit tests for the {@link JobStorage} persistence layer.
 */
class JobStorageTest {
    private Path tempRoot;
    private ServletContext servletContext;
    private JobStorage jobStorage;

    /**
     * Sets up a temporary directory and initializes JobStorage before each test.
     *
     * @throws IOException if the temporary directory cannot be created.
     */
    @BeforeEach
    void setUp() throws IOException {
        tempRoot = Files.createTempDirectory("job-storage-test");
        servletContext = TestUtils.createServletContext(tempRoot);
        jobStorage = new JobStorage(servletContext);
    }

    /**
     * Cleans up the temporary directory after each test.
     *
     * @throws IOException if file cleanup fails.
     */
    @AfterEach
    void tearDown() throws IOException {
        if (tempRoot != null) {
            Files.walk(tempRoot)
                    .sorted((a, b) -> b.compareTo(a))
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    /**
     * Tests creating a new job and verifying it is persisted and retrievable by ID.
     */
    @Test
    void createNewJobPersistsAndCanBeFound() {
        Job job = jobStorage.createNew("TA Helper", "CS200", "10h", "Java", LocalDate.now().plusDays(10), "mo@test.com");
        assertNotNull(job.getId());
        assertEquals("TA Helper", job.getTitle());

        Job loaded = jobStorage.findById(job.getId());
        assertNotNull(loaded);
        assertEquals(job.getTitle(), loaded.getTitle());
        assertEquals(Job.STATUS_OPEN, loaded.getStatus());
    }

    /**
     * Tests that findAll returns all saved jobs.
     */
    @Test
    void findAllReturnsSavedJobs() {
        jobStorage.createNew("Job 1", "CS300", "5h", "Python", LocalDate.now().plusDays(5), "mo@test.com");
        jobStorage.createNew("Job 2", "CS301", "6h", "C++", LocalDate.now().plusDays(6), "mo@test.com");

        List<Job> all = jobStorage.findAll();
        assertEquals(2, all.size());
    }

    /**
     * Tests archiving a job and verifying the status is updated to ARCHIVED.
     */
    @Test
    void archiveJobUpdatesStatusToArchived() {
        Job job = jobStorage.createNew("Job Archive", "CS302", "4h", "Algorithms", LocalDate.now().plusDays(7), "mo@test.com");
        jobStorage.archive(job.getId());

        Job updated = jobStorage.findById(job.getId());
        assertNotNull(updated);
        assertEquals(Job.STATUS_ARCHIVED, updated.getStatus());
    }

    /**
     * Tests updating an existing job and verifying the changes are persisted.
     */
    @Test
    void updateReplacesExistingJob() {
        Job job = jobStorage.createNew("Job Replace", "CS303", "3h", "Database", LocalDate.now().plusDays(8), "mo@test.com");
        job.setTitle("Job Replace Updated");
        jobStorage.update(job);

        Job updated = jobStorage.findById(job.getId());
        assertEquals("Job Replace Updated", updated.getTitle());
    }

    /**
     * Tests that malformed data lines are skipped during job loading.
     *
     * @throws IOException if file operations fail.
     */
    @Test
    void malformedJobLinesAreSkipped() throws IOException {
        Path storagePath = Path.of(servletContext.getRealPath("WEB-INF/data/jobs.txt"));
        Files.writeString(storagePath, "bad|line|without|enough|fields\n");
        List<Job> jobs = jobStorage.findAll();
        assertTrue(jobs.isEmpty());
    }

    /**
     * Tests that jobs with invalid dates are skipped during loading.
     *
     * @throws IOException if file operations fail.
     */
    @Test
    void jobWithInvalidDatesIsSkipped() throws IOException {
        Path storagePath = Path.of(servletContext.getRealPath("WEB-INF/data/jobs.txt"));
        String badLine = "id|title|code|workload|req|invalid-date|poster|2026-05-23T00:00:00Z|Open|2026-05-23T00:00:00Z";
        Files.writeString(storagePath, badLine + "\n");
        List<Job> jobs = jobStorage.findAll();
        assertTrue(jobs.isEmpty());
    }
}
