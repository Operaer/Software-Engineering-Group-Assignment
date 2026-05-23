package com.bupt.ta.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link AuditLogEntry} model class.
 */
class AuditLogEntryTest {

    /**
     * Verifies the constructor correctly sets all required fields and generates an ID and timestamp.
     */
    @Test
    void constructorSetsRequiredFields() {
        AuditLogEntry entry = new AuditLogEntry("operator@example.com", "Login", "target", "User login event");
        assertNotNull(entry.getId());
        assertNotNull(entry.getTimestamp());
        assertEquals("operator@example.com", entry.getOperator());
        assertEquals("Login", entry.getActionType());
        assertEquals("target", entry.getTarget());
        assertEquals("User login event", entry.getDescription());
    }

    /**
     * Tests that all setter and getter methods work correctly.
     */
    @Test
    void settersAndGettersWork() {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setId("entry-1");
        entry.setTimestamp(entry.getTimestamp());
        entry.setOperator("mo@example.com");
        entry.setActionType("Create");
        entry.setTarget("job-1");
        entry.setDescription("Created job");

        assertEquals("entry-1", entry.getId());
        assertEquals("mo@example.com", entry.getOperator());
        assertEquals("Create", entry.getActionType());
        assertEquals("job-1", entry.getTarget());
        assertEquals("Created job", entry.getDescription());
    }

    /**
     * Ensures each audit log entry receives a unique generated ID.
     */
    @Test
    void idIsUniqueForDifferentEntries() {
        AuditLogEntry first = new AuditLogEntry("op1", "Action", "t1", "desc1");
        AuditLogEntry second = new AuditLogEntry("op2", "Action", "t2", "desc2");
        assertNotEquals(first.getId(), second.getId());
    }
}
