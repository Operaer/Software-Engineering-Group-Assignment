package com.bupt.ta.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bupt.ta.TestUtils;
import com.bupt.ta.model.Application;

/**
 * Unit tests for the {@link ApplicationStorage} persistence layer.
 */
class ApplicationStorageTest {
    private Path tempRoot;
    private ServletContext servletContext;
    private ApplicationStorage applicationStorage;

    /**
     * Sets up a temporary directory and initializes ApplicationStorage before each test.
     *
     * @throws IOException if the temporary directory cannot be created.
     */
    @BeforeEach
    void setUp() throws IOException {
        tempRoot = Files.createTempDirectory("application-storage-test");
        servletContext = TestUtils.createServletContext(tempRoot);
        applicationStorage = new ApplicationStorage(servletContext);
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
     * Tests creating a new application and verifying it is persisted and retrievable.
     */
    @Test
    void createNewApplicationPersistsAndCanBeFound() {
        Application application = applicationStorage.createNew("ta@example.com", "job-1", "TA Position");
        assertNotNull(application.getId());
        assertTrue(applicationStorage.hasApplied("ta@example.com", "job-1"));

        List<Application> found = applicationStorage.findByTaEmail("ta@example.com");
        assertEquals(1, found.size());
        assertEquals("Pending", found.get(0).getStatus());
    }

    /**
     * Verifies that searching by TA email is case-insensitive.
     */
    @Test
    void findByTaEmailIsCaseInsensitive() {
        applicationStorage.createNew("TA@example.com", "job-2", "Tutor");
        List<Application> found = applicationStorage.findByTaEmail("ta@example.com");
        assertEquals(1, found.size());
    }

    /**
     * Tests that hasApplied returns false when given null inputs.
     */
    @Test
    void hasAppliedReturnsFalseForNullInputs() {
        assertFalse(applicationStorage.hasApplied(null, "job-1"));
        assertFalse(applicationStorage.hasApplied("ta@example.com", null));
    }

    /**
     * Tests updating an application's status and verifying the change persists.
     */
    @Test
    void updateStatusChangesApplicationStatuses() {
        Application application = applicationStorage.createNew("ta@example.com", "job-3", "Lab");
        String id = application.getId();
        applicationStorage.updateStatus(id, Application.Status.Accepted, "mo@example.com");

        Application updated = applicationStorage.findByTaEmail("ta@example.com").get(0);
        assertEquals(Application.Status.Accepted.name(), updated.getStatus());
    }

    /**
     * Tests bulk status update for multiple applications at once.
     */
    @Test
    void bulkUpdateStatusUpdatesMultipleApplications() {
        Application a1 = applicationStorage.createNew("ta1@example.com", "job-4", "Position 1");
        Application a2 = applicationStorage.createNew("ta2@example.com", "job-4", "Position 1");

        applicationStorage.updateStatus(List.of(a1.getId(), a2.getId()), Application.Status.Shortlisted, "mo@example.com");

        assertEquals(Application.Status.Shortlisted.name(), applicationStorage.findByTaEmail("ta1@example.com").get(0).getStatus());
        assertEquals(Application.Status.Shortlisted.name(), applicationStorage.findByTaEmail("ta2@example.com").get(0).getStatus());
    }

    /**
     * Tests updating the assigned workload hours for an application.
     */
    @Test
    void updateAssignedWorkloadStoresValue() {
        Application application = applicationStorage.createNew("ta@example.com", "job-5", "Position");
        applicationStorage.updateAssignedWorkload(application.getId(), 15, "admin@example.com");

        Application updated = applicationStorage.findByTaEmail("ta@example.com").get(0);
        assertEquals(15, updated.getAssignedWorkloadHours());
    }

    /**
     * Verifies that updating an unknown application ID has no effect.
     */
    @Test
    void updateAssignedWorkloadWithUnknownIdDoesNothing() {
        applicationStorage.updateAssignedWorkload("unknown-id", 5, "system");
        assertTrue(applicationStorage.findAll().isEmpty());
    }

    /**
     * Tests that old pending applications are automatically marked as expired.
     *
     * @throws IOException if file operations fail.
     */
    @Test
    void expiredApplicationsAreAutomaticallyMarkedExpired() throws IOException {
        Application oldApp = new Application();
        oldApp.setId("old-id");
        oldApp.setTaEmail("ta@example.com");
        oldApp.setPositionId("job-old");
        oldApp.setPositionTitle("Old Position");
        oldApp.setAppliedAt(Instant.now().minus(10, ChronoUnit.DAYS));
        oldApp.setStatus(Application.Status.Pending.name());
        applicationStorage.save(oldApp);

        List<Application> loaded = applicationStorage.findByTaEmail("ta@example.com");
        assertEquals(1, loaded.size());
        assertEquals(Application.Status.Expired.name(), loaded.get(0).getStatus());
    }
}
