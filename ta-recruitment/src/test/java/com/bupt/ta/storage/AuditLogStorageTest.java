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
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bupt.ta.TestUtils;
import com.bupt.ta.model.AuditLogEntry;

/**
 * Unit tests for the {@link AuditLogStorage} persistence layer.
 */
class AuditLogStorageTest {
    private Path tempRoot;
    private ServletContext servletContext;
    private AuditLogStorage auditLogStorage;

    /**
     * Sets up a temporary directory and initializes AuditLogStorage before each test.
     *
     * @throws IOException if the temporary directory cannot be created.
     */
    @BeforeEach
    void setUp() throws IOException {
        tempRoot = Files.createTempDirectory("audit-log-storage-test");
        servletContext = TestUtils.createServletContext(tempRoot);
        auditLogStorage = new AuditLogStorage(servletContext);
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
     * Tests adding audit entries and querying them by operator and action type.
     */
    @Test
    void addAndQueryAuditEntries() {
        auditLogStorage.add(new AuditLogEntry("admin@example.com", "Create", "job-1", "Created job"));
        auditLogStorage.add(new AuditLogEntry("mo@example.com", "Review", "app-1", "Reviewed application"));

        List<AuditLogEntry> all = auditLogStorage.query(null, null, null, null);
        assertEquals(2, all.size());

        List<AuditLogEntry> filteredByOperator = auditLogStorage.query("admin", null, null, null);
        assertEquals(1, filteredByOperator.size());
        assertEquals("admin@example.com", filteredByOperator.get(0).getOperator());

        List<AuditLogEntry> filteredByAction = auditLogStorage.query(null, "Review", null, null);
        assertEquals(1, filteredByAction.size());
        assertEquals("Review", filteredByAction.get(0).getActionType());
    }

    /**
     * Tests that findOperators and findActionTypes return sorted unique values.
     */
    @Test
    void findOperatorsAndActionTypesReturnSortedUniqueValues() {
        auditLogStorage.add(new AuditLogEntry("b@example.com", "Update", "job-2", "Updated job"));
        auditLogStorage.add(new AuditLogEntry("a@example.com", "Update", "job-3", "Updated job"));
        auditLogStorage.add(new AuditLogEntry("a@example.com", "Create", "job-4", "Created job"));

        List<String> operators = auditLogStorage.findOperators();
        assertEquals(List.of("a@example.com", "b@example.com"), operators);

        List<String> actionTypes = auditLogStorage.findActionTypes();
        assertEquals(List.of("Create", "Update"), actionTypes);
    }

    /**
     * Tests querying audit entries within a specific date range.
     */
    @Test
    void queryWithDateRangeFiltersResults() {
        auditLogStorage.add(new AuditLogEntry("admin@example.com", "Create", "job-5", "Created job"));
        Instant from = Instant.now().minusSeconds(5);
        List<AuditLogEntry> entries = auditLogStorage.query(null, null, from, Instant.now().plusSeconds(5));
        assertFalse(entries.isEmpty());
    }
}
