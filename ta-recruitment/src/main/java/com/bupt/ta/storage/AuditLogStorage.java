package com.bupt.ta.storage;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.AuditLogEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON file storage manager for audit logs.
 * <p>Responsible for recording audit logs of user operations in the system, supporting log addition,
 * querying, and automatic cleanup (logs older than one year are automatically pruned).</p>
 */
public class AuditLogStorage {
    private final File storageFile;
    private final ObjectMapper mapper;

    /**
     * Constructs an AuditLogStorage instance, initializes the storage file and ensures the file
     * exists and contains a valid JSON array.
     *
     * @param context Servlet context, used to obtain the real path of the storage file
     */
    public AuditLogStorage(ServletContext context) {
        this.storageFile = new File(context.getRealPath(AppConfig.AUDIT_LOG_FILE));
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule()).enable(SerializationFeature.INDENT_OUTPUT);

        try {
            File parent = storageFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            if (!storageFile.exists()) storageFile.createNewFile();
            // ensure file contains JSON array
            if (storageFile.length() == 0) {
                mapper.writeValue(storageFile, new ArrayList<AuditLogEntry>());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize audit log storage", e);
        }
    }

    /**
     * Loads all audit log entries from the file.
     *
     * @return the list of audit log entries
     */
    private List<AuditLogEntry> loadAll() {
        try {
            List<AuditLogEntry> list = mapper.readValue(storageFile, new TypeReference<List<AuditLogEntry>>() {});
            return list == null ? new ArrayList<>() : list;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read audit log storage", e);
        }
    }

    /**
     * Writes all audit log entries to the storage file.
     *
     * @param entries the list of audit log entries to save
     */
    private void saveAll(List<AuditLogEntry> entries) {
        try {
            mapper.writeValue(storageFile, entries);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write audit log storage", e);
        }
    }

    /**
     * Adds an audit log entry (thread-safe) and automatically prunes logs older than one year.
     *
     * @param entry the audit log entry to add
     */
    public synchronized void add(AuditLogEntry entry) {
        List<AuditLogEntry> entries = loadAll();
        entries.add(entry);
        pruneOld(entries);
        saveAll(entries);
    }

    /**
     * Prunes old audit log entries older than one year.
     *
     * @param entries the list of log entries to prune
     */
    private void pruneOld(List<AuditLogEntry> entries) {
        Instant cutoff = Instant.now().minus(365, ChronoUnit.DAYS);
        Iterator<AuditLogEntry> it = entries.iterator();
        while (it.hasNext()) {
            AuditLogEntry e = it.next();
            if (e.getTimestamp() != null && e.getTimestamp().isBefore(cutoff)) {
                it.remove();
            }
        }
    }

    /**
     * Queries audit logs by operator, action type, and time range.
     *
     * @param operator   the operator (supports fuzzy matching; ignored if null or blank)
     * @param actionType the action type (case-insensitive; ignored if null or blank)
     * @param from       the start time (inclusive; ignored if null)
     * @param to         the end time (inclusive; ignored if null)
     * @return the list of matching audit log entries
     */
    public List<AuditLogEntry> query(String operator, String actionType, Instant from, Instant to) {
        return loadAll().stream()
                .filter(e -> operator == null || operator.isBlank() || (e.getOperator() != null && e.getOperator().contains(operator)))
                .filter(e -> actionType == null || actionType.isBlank() || (e.getActionType() != null && e.getActionType().equalsIgnoreCase(actionType)))
                .filter(e -> from == null || (e.getTimestamp() != null && !e.getTimestamp().isBefore(from)))
                .filter(e -> to == null || (e.getTimestamp() != null && !e.getTimestamp().isAfter(to)))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all distinct operators (sorted alphabetically).
     *
     * @return the sorted list of distinct operators
     */
    public List<String> findOperators() {
        return loadAll().stream()
                .map(AuditLogEntry::getOperator)
                .filter(o -> o != null && !o.isBlank())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all distinct action types (sorted alphabetically).
     *
     * @return the sorted list of distinct action types
     */
    public List<String> findActionTypes() {
        return loadAll().stream()
                .map(AuditLogEntry::getActionType)
                .filter(a -> a != null && !a.isBlank())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
    }
}
