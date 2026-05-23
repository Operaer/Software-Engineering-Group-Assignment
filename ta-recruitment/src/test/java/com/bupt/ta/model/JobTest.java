package com.bupt.ta.model;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Job} model class.
 */
class JobTest {

    /**
     * Verifies that a newly created Job defaults to OPEN status.
     */
    @Test
    void defaultStatusIsOpenAfterConstructor() {
        Job job = new Job("1", "TA Support", "CS101", "5h", "Java", LocalDate.now(), "mo@example.com", Instant.now());
        assertEquals(Job.STATUS_OPEN, job.getStatus());
    }

    /**
     * Tests all setter and getter methods for roundtrip correctness.
     */
    @Test
    void settersAndGettersRoundtrip() {
        Job job = new Job();
        job.setId("123");
        job.setTitle("Research Assistant");
        job.setModuleCode("CS102");
        job.setWorkload("3h");
        job.setRequirements("C programming");
        job.setDeadline(LocalDate.of(2026, 12, 31));
        job.setPostedBy("mo@example.com");
        job.setPostedAt(Instant.parse("2026-05-23T10:00:00Z"));
        job.setUpdatedAt(Instant.parse("2026-05-23T11:00:00Z"));
        job.setStatus(Job.STATUS_CLOSED);

        assertEquals("123", job.getId());
        assertEquals("Research Assistant", job.getTitle());
        assertEquals("CS102", job.getModuleCode());
        assertEquals("3h", job.getWorkload());
        assertEquals("C programming", job.getRequirements());
        assertEquals(LocalDate.of(2026, 12, 31), job.getDeadline());
        assertEquals("mo@example.com", job.getPostedBy());
        assertEquals(Instant.parse("2026-05-23T10:00:00Z"), job.getPostedAt());
        assertEquals(Instant.parse("2026-05-23T11:00:00Z"), job.getUpdatedAt());
        assertEquals(Job.STATUS_CLOSED, job.getStatus());
    }

    /**
     * Ensures the job status constants are distinct from each other.
     */
    @Test
    void statusConstantsAreDistinct() {
        assertNotEquals(Job.STATUS_OPEN, Job.STATUS_ARCHIVED);
        assertNotEquals(Job.STATUS_ARCHIVED, Job.STATUS_CLOSED);
        assertNotEquals(Job.STATUS_OPEN, Job.STATUS_CLOSED);
    }

    /**
     * Verifies that updatedAt defaults to the postedAt value when constructed.
     */
    @Test
    void updatedAtDefaultsToPostedAtWhenConstructed() {
        Instant postedAt = Instant.now();
        Job job = new Job("id", "title", "code", "workload", "req", LocalDate.now(), "mo", postedAt);
        assertEquals(postedAt, job.getUpdatedAt());
    }
}
