package com.bupt.ta.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.Application;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON-backed storage for TA applications.
 */
public class ApplicationStorage {
    private static final String STORAGE_PATH = AppConfig.APPLICATIONS_FILE;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final File storageFile;
    private final javax.servlet.ServletContext servletContext;

    public ApplicationStorage(ServletContext context) {
        this.storageFile = new File(context.getRealPath(STORAGE_PATH));
        this.servletContext = context;
        ensureStorageExists();
    }

    private void ensureStorageExists() {
        try {
            File parent = storageFile.getParentFile();
            if (!parent.exists()) {
                Files.createDirectories(parent.toPath());
            }
            if (!storageFile.exists()) {
                mapper.writeValue(storageFile, new ArrayList<>());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize application storage", e);
        }
    }

    private static final Duration EXPIRATION_PERIOD = Duration.ofDays(7);

    private List<Application> loadAll() {
        try {
            TypeFactory factory = mapper.getTypeFactory();
            CollectionType listType = factory.constructCollectionType(ArrayList.class, Application.class);
            List<Application> list = mapper.readValue(storageFile, listType);
            if (list == null) {
                list = new ArrayList<>();
            }
            checkAndUpdateExpiredApplications(list);
            return list;
        } catch (JsonProcessingException e) {
            saveAll(new ArrayList<>());
            return new ArrayList<>();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read applications storage", e);
        }
    }

    private void checkAndUpdateExpiredApplications(List<Application> applications) {
        Instant now = Instant.now();
        boolean hasChanges = false;
        
        for (Application app : applications) {
            if (app.getAppliedAt() != null && app.getStatus() != null) {
                String status = app.getStatus();
                if (!status.equals(Application.Status.Accepted.name()) 
                    && !status.equals(Application.Status.Rejected.name())
                    && !status.equals(Application.Status.Expired.name())) {
                    
                    Duration age = Duration.between(app.getAppliedAt(), now);
                    if (age.compareTo(EXPIRATION_PERIOD) > 0) {
                        app.setStatus(Application.Status.Expired.name());
                        hasChanges = true;
                    }
                }
            }
        }
        
        if (hasChanges) {
            saveAll(applications);
        }
    }

    private void saveAll(List<Application> list) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(storageFile, list);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write applications storage", e);
        }
    }

    public List<Application> findByTaEmail(String email) {
        if (email == null) {
            return new ArrayList<>();
        }
        String normalized = email.toLowerCase();
        return loadAll().stream()
                .filter(app -> normalized.equals(app.getTaEmail()))
                .collect(Collectors.toList());
    }

    public void save(Application application) {
        List<Application> apps = loadAll();
        apps.add(application);
        saveAll(apps);
        try {
            com.bupt.ta.storage.AuditLogStorage audit = new com.bupt.ta.storage.AuditLogStorage(servletContext);
            audit.add(new com.bupt.ta.model.AuditLogEntry(application.getTaEmail(), "Application Create", application.getId(), "Applied to: " + application.getPositionTitle()));
        } catch (Exception ignored) {}
    }

    public boolean hasApplied(String taEmail, String positionId) {
        if (taEmail == null || positionId == null) {
            return false;
        }
        String normalizedEmail = taEmail.toLowerCase();
        return loadAll().stream()
                .anyMatch(app -> normalizedEmail.equals(app.getTaEmail()) && positionId.equals(app.getPositionId()));
    }

    public Application createNew(String taEmail, String positionId, String positionTitle) {
        Application app = new Application();
        app.setId(UUID.randomUUID().toString());
        app.setTaEmail(taEmail.toLowerCase());
        app.setPositionId(positionId);
        app.setPositionTitle(positionTitle);
        app.setAppliedAt(Instant.now());
        app.setStatus("Pending");
        save(app);
        return app;
    }

    /**
     * Updates the status of a single application using the default "system" operator.
     * This method delegates to {@link #updateStatus(String, Application.Status, String)}.
     *
     * @param applicationId the unique identifier of the application to update
     * @param status the new status for the application
     */
    public void updateStatus(String applicationId, Application.Status status) {
        updateStatus(applicationId, status, "system");
    }

    /**
     * Updates the status of a single application and records the change in the audit log.
     * This method delegates to the bulk update method for consistency.
     *
     * @param applicationId the unique identifier of the application to update
     * @param status the new status for the application
     * @param operator the user email or identifier performing this operation (recorded in audit log)
     */
    public void updateStatus(String applicationId, Application.Status status, String operator) {
        updateStatus(Collections.singletonList(applicationId), status, operator);
    }

    /**
     * Bulk updates the status for multiple applications in a single operation.
     * All status changes are persisted to storage and an audit log entry is created
     * for each application updated, with the given operator identifier.
     *
     * @param applicationIds list of application identifiers to update; if null or empty, operation is skipped
     * @param status the new status to apply to all selected applications
     * @param operator the user email or identifier performing this bulk update (recorded in audit log);
     *                 defaults to "system" if null
     */
    public void updateStatus(List<String> applicationIds, Application.Status status, String operator) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return;
        }

        List<Application> apps = loadAll();
        HashSet<String> idSet = new HashSet<>(applicationIds);
        for (Application app : apps) {
            if (idSet.contains(app.getId())) {
                app.setStatus(status.name());
            }
        }
        saveAll(apps);

        try {
            com.bupt.ta.storage.AuditLogStorage audit = new com.bupt.ta.storage.AuditLogStorage(servletContext);
            String operatorId = operator != null ? operator : "system";
            for (String applicationId : applicationIds) {
                audit.add(new com.bupt.ta.model.AuditLogEntry(operatorId, "Application Status", applicationId, "Status changed to: " + status.name()));
            }
        } catch (Exception ignored) {}
    }

    public List<Application> findAll() {
        return loadAll();
    }
}

