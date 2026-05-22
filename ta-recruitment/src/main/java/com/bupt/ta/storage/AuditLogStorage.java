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
 * JSON-backed storage for audit log entries.
 *
 * <p>This class manages persisted audit log records, providing methods to add,
 * query, and prune entries. Each entry records an action performed by an operator
 * on a particular resource.</p>
 *
 * @author Operaer
 * @date 2026-05-17
 */
public class AuditLogStorage {
    private final File storageFile;
    private final ObjectMapper mapper;

    /**
     * Constructs an {@code AuditLogStorage} instance and initialises the underlying
     * JSON storage file. Creates parent directories and the file itself if they do
     * not exist, writing an empty JSON array when the file is first created.
     *
     * @param context the ServletContext used to resolve the real path to the storage file
     * @throws IllegalStateException if the storage file cannot be initialised
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
     * Loads all audit log entries from the JSON storage file.
     *
     * @return a mutable list of all stored entries (never {@code null})
     * @throws IllegalStateException if the storage file cannot be read
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
     * Persists the given list of audit log entries to the JSON storage file.
     *
     * @param entries the entries to write (must not be {@code null})
     * @throws IllegalStateException if the file cannot be written
     */
    private void saveAll(List<AuditLogEntry> entries) {
        try {
            mapper.writeValue(storageFile, entries);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write audit log storage", e);
        }
    }

    /**
     * Adds a new audit log entry. Old entries beyond the retention period are
     * automatically pruned before persisting. This method is synchronised to
     * provide basic thread safety.
     *
     * @param entry the entry to add (must not be {@code null})
     */
    public synchronized void add(AuditLogEntry entry) {
        List<AuditLogEntry> entries = loadAll();
        entries.add(entry);
        pruneOld(entries);
        saveAll(entries);
    }

    /**
     * Removes entries from the list whose timestamp is older than 365 days from now.
     *
     * @param entries the list to prune (modified in place)
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
     * Queries audit log entries with optional filters. Each filter is applied only
     * when its value is non-null and non-blank. The operator filter performs a
     * substring match (case-sensitive), while the action type filter is
     * case-insensitive.
     *
     * @param operator   optional operator substring to match; may be {@code null}
     * @param actionType optional action type to match (case-insensitive); may be {@code null}
     * @param from       optional start of the time range (inclusive); may be {@code null}
     * @param to         optional end of the time range (inclusive); may be {@code null}
     * @return a list of matching entries (never {@code null})
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
     * Returns a sorted, deduplicated list of all operator identifiers found in the
     * audit log. Empty or blank operators are excluded.
     *
     * @return distinct operator identifiers, sorted case-insensitively (never {@code null})
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
     * Returns a sorted, deduplicated list of all action types found in the audit
     * log. Empty or blank action types are excluded.
     *
     * @return distinct action type strings, sorted case-insensitively (never {@code null})
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
