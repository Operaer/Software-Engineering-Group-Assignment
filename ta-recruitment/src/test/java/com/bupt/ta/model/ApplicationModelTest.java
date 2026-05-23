package com.bupt.ta.model;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Application} model class.
 */
class ApplicationModelTest {

    /**
     * Verifies all fields are correctly set and retrieved after construction.
     */
    @Test
    void applicationFieldsRoundtrip() {
        Application application = new Application("app-1", "ta@example.com", "job-1", "TA Tutor", Instant.now(), Application.Status.Pending.name());
        application.setAssignedWorkloadHours(12);

        assertEquals("app-1", application.getId());
        assertEquals("ta@example.com", application.getTaEmail());
        assertEquals("job-1", application.getPositionId());
        assertEquals("TA Tutor", application.getPositionTitle());
        assertEquals(Application.Status.Pending.name(), application.getStatus());
        assertEquals(12, application.getAssignedWorkloadHours());
    }

    /**
     * Tests setting and clearing the assigned workload hours field.
     */
    @Test
    void canSetAndClearAssignedWorkloadHours() {
        Application application = new Application();
        application.setAssignedWorkloadHours(20);
        assertEquals(20, application.getAssignedWorkloadHours());
        application.setAssignedWorkloadHours(null);
        assertNull(application.getAssignedWorkloadHours());
    }

    /**
     * Verifies that the {@link Application.Status} enum contains the expected values.
     */
    @Test
    void statusEnumContainsExpectedValues() {
        assertEquals("Pending", Application.Status.Pending.name());
        assertEquals("Accepted", Application.Status.Accepted.name());
        assertEquals("Expired", Application.Status.Expired.name());
    }

    /**
     * Tests that all mutable fields can be updated via their setter methods.
     */
    @Test
    void canMutateFields() {
        Application application = new Application();
        application.setId("id-111");
        application.setTaEmail("bob@example.com");
        application.setPositionId("job-222");
        application.setPositionTitle("Lab Helper");
        Instant now = Instant.now();
        application.setAppliedAt(now);
        application.setStatus(Application.Status.Shortlisted.name());

        assertEquals("id-111", application.getId());
        assertEquals("bob@example.com", application.getTaEmail());
        assertEquals("job-222", application.getPositionId());
        assertEquals("Lab Helper", application.getPositionTitle());
        assertEquals(now, application.getAppliedAt());
        assertEquals(Application.Status.Shortlisted.name(), application.getStatus());
    }
}
