package com.bupt.ta.storage;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.OperationLogEntry;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistent storage of administrative operation logs.
 * Logs are written to a file and can be retrieved for display in the
 * audit log viewer.
 */
public class OperationLogStorage {
    private static final String STORAGE_PATH = AppConfig.OPERATION_LOG_FILE;
    private final File storageFile;

    /**
     * Creates a storage helper for operation logs using the servlet context.
     *
     * @param context Servlet context for resolving data file locations
     */
    public OperationLogStorage(ServletContext context) {
        this.storageFile = new File(context.getRealPath(STORAGE_PATH));
        ensureStorageExists();
    }

    private void ensureStorageExists() {
        try {
            File parent = storageFile.getParentFile();
            if (!parent.exists()) {
                Files.createDirectories(parent.toPath());
            }
            if (!storageFile.exists()) {
                storageFile.createNewFile();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize operation log storage", e);
        }
    }

    /**
     * Appends a new operation log entry to persistent storage.
     *
     * @param operator the user who performed the operation
     * @param actionType the type of action performed
     * @param target the target object or resource identifier
     * @param details additional details about the operation
     */
    public void record(String operator, String actionType, String target, String details) {
        String encodedOperator = operator == null ? "" : URLEncoder.encode(operator, StandardCharsets.UTF_8);
        String encodedActionType = actionType == null ? "" : URLEncoder.encode(actionType, StandardCharsets.UTF_8);
        String encodedTarget = target == null ? "" : URLEncoder.encode(target, StandardCharsets.UTF_8);
        String encodedDetails = details == null ? "" : URLEncoder.encode(details, StandardCharsets.UTF_8);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile, true))) {
            writer.write(encodedOperator + "|" + encodedActionType + "|" + encodedTarget + "|" + Instant.now().toString() + "|" + encodedDetails);
            writer.newLine();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write operation log storage", e);
        }
    }

    /**
     * Reads all persisted operation log entries from storage.
     *
     * @return a list of deserialized log entries
     */
    public List<OperationLogEntry> findAll() {
        List<OperationLogEntry> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 5);
                if (parts.length >= 5) {
                    OperationLogEntry entry = new OperationLogEntry();
                    entry.setOperator(URLDecoder.decode(parts[0], StandardCharsets.UTF_8));
                    entry.setActionType(URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
                    entry.setTarget(URLDecoder.decode(parts[2], StandardCharsets.UTF_8));
                    entry.setTimestamp(Instant.parse(parts[3]));
                    entry.setDetails(URLDecoder.decode(parts[4], StandardCharsets.UTF_8));
                    entries.add(entry);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read operation log storage", e);
        }
        return entries;
    }
}
