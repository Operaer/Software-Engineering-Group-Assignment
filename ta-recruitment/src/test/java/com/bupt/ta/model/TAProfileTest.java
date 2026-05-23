package com.bupt.ta.model;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link TAProfile} model class.
 */
class TAProfileTest {

    /**
     * Verifies all profile fields are correctly set and retrieved.
     */
    @Test
    void profileFieldsRoundtrip() {
        TAProfile profile = new TAProfile("ta@example.com");
        profile.setName("Alice");
        profile.setStudentId("20261234");
        profile.setMajor("Computer Science");
        profile.setPhone("+8613800000000");
        profile.setGpa(3.95);
        profile.setSkills(Arrays.asList("Java", "Data Structures"));
        profile.setResumeFileName("resume.pdf");

        assertEquals("ta@example.com", profile.getEmail());
        assertEquals("Alice", profile.getName());
        assertEquals("20261234", profile.getStudentId());
        assertEquals("Computer Science", profile.getMajor());
        assertEquals("+8613800000000", profile.getPhone());
        assertEquals(3.95, profile.getGpa());
        assertEquals(Arrays.asList("Java", "Data Structures"), profile.getSkills());
        assertEquals("resume.pdf", profile.getResumeFileName());
    }

    /**
     * Ensures the skills list is never null after construction.
     */
    @Test
    void emptySkillsListIsInitialized() {
        TAProfile profile = new TAProfile("ta@example.com");
        assertNotNull(profile.getSkills());
        assertTrue(profile.getSkills().isEmpty());
    }

    /**
     * Tests replacing the entire skills list with new values.
     */
    @Test
    void canReplaceSkillsList() {
        TAProfile profile = new TAProfile("ta@example.com");
        profile.setSkills(Arrays.asList("Python", "Algorithms"));
        assertEquals(2, profile.getSkills().size());
        assertTrue(profile.getSkills().contains("Python"));
    }

    /**
     * Tests that the GPA field can be set to null.
     */
    @Test
    void gpaIsNullable() {
        TAProfile profile = new TAProfile("ta@example.com");
        profile.setGpa(null);
        assertNull(profile.getGpa());
    }
}
