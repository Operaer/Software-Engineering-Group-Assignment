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

public class AuditLogStorage {
    private final File storageFile;
    private final ObjectMapper mapper;

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

    private List<AuditLogEntry> loadAll() {
        try {
            List<AuditLogEntry> list = mapper.readValue(storageFile, new TypeReference<List<AuditLogEntry>>() {});
            return list == null ? new ArrayList<>() : list;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read audit log storage", e);
        }
    }

    private void saveAll(List<AuditLogEntry> entries) {
        try {
            mapper.writeValue(storageFile, entries);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write audit log storage", e);
        }
    }

    public synchronized void add(AuditLogEntry entry) {
        List<AuditLogEntry> entries = loadAll();
        entries.add(entry);
        pruneOld(entries);
        saveAll(entries);
    }

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

    public List<AuditLogEntry> query(String operator, String actionType, Instant from, Instant to) {
        return loadAll().stream()
                .filter(e -> operator == null || operator.isBlank() || (e.getOperator() != null && e.getOperator().contains(operator)))
                .filter(e -> actionType == null || actionType.isBlank() || (e.getActionType() != null && e.getActionType().equalsIgnoreCase(actionType)))
                .filter(e -> from == null || (e.getTimestamp() != null && !e.getTimestamp().isBefore(from)))
                .filter(e -> to == null || (e.getTimestamp() != null && !e.getTimestamp().isAfter(to)))
                .collect(Collectors.toList());
    }

    public List<String> findOperators() {
        return loadAll().stream()
                .map(AuditLogEntry::getOperator)
                .filter(o -> o != null && !o.isBlank())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
    }

    public List<String> findActionTypes() {
        return loadAll().stream()
                .map(AuditLogEntry::getActionType)
                .filter(a -> a != null && !a.isBlank())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
    }
}
