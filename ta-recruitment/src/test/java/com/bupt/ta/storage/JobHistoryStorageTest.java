package com.bupt.ta.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bupt.ta.TestUtils;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.JobHistoryEntry;

/**
 * Unit tests for the {@link JobHistoryStorage} persistence layer.
 */
class JobHistoryStorageTest {
    private Path tempRoot;
    private ServletContext servletContext;
    private JobHistoryStorage jobHistoryStorage;

    /**
     * Sets up a temporary directory and initializes JobHistoryStorage before each test.
     *
     * @throws IOException if the temporary directory cannot be created.
     */
    @BeforeEach
    void setUp() throws IOException {
        tempRoot = Files.createTempDirectory("job-history-storage-test");
        servletContext = TestUtils.createServletContext(tempRoot);
        jobHistoryStorage = new JobHistoryStorage(servletContext);
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
     * Tests recording job history without a snapshot and verifying the entry is persisted.
     */
    @Test
    void recordWithoutSnapshotPersistedAndCanBeQueried() {
        jobHistoryStorage.record("job-1", "Update", "Changed workload", "mo@example.com");
        List<JobHistoryEntry> entries = jobHistoryStorage.findByJobId("job-1");
        assertEquals(1, entries.size());
        assertEquals("Update", entries.get(0).getAction());
        assertEquals("Changed workload", entries.get(0).getDetails());
    }

    /**
     * Tests recording job history with a full job snapshot and retrieving the last snapshot.
     */
    @Test
    void recordWithSnapshotCanReturnLastSnapshot() {
        Job job = new Job("job-2", "TA Tutor", "CS200", "6h", "Java", null, "mo@example.com", Instant.now());
        jobHistoryStorage.record("job-2", "Edit", "Snapshot stored", job, "mo@example.com");
        Job snapshot = jobHistoryStorage.findLastSnapshot("job-2");
        assertNotNull(snapshot);
        assertEquals("TA Tutor", snapshot.getTitle());
    }

    /**
     * Tests finding a history entry by job ID and changed-at timestamp.
     */
    @Test
    void findByJobIdAndChangedAtReturnsCorrectEntry() {
        jobHistoryStorage.record("job-3", "Create", "Created job", "admin@example.com");
        List<JobHistoryEntry> entries = jobHistoryStorage.findByJobId("job-3");
        assertEquals(1, entries.size());
        JobHistoryEntry entry = entries.get(0);
        JobHistoryEntry found = jobHistoryStorage.findByJobIdAndChangedAt("job-3", entry.getChangedAt());
        assertNotNull(found);
        assertEquals(entry.getAction(), found.getAction());
    }

    /**
     * Tests that find returns null when the entry does not exist.
     */
    @Test
    void findByJobIdAndChangedAtReturnsNullWhenMissing() {
        assertNull(jobHistoryStorage.findByJobIdAndChangedAt("missing", Instant.now()));
    }
}
