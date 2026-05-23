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
 * JSON file storage manager for TA application data.
 * <p>Responsible for persisting, loading, and querying application records in the TA recruitment system.
 * Data is stored in JSON format in server-side files, with automatic expiration handling of application status.</p>
 *
 * @author Operaer
 */
public class ApplicationStorage {
    private static final String STORAGE_PATH = AppConfig.APPLICATIONS_FILE;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final File storageFile;
    private final javax.servlet.ServletContext servletContext;

    /**
     * Constructs an ApplicationStorage instance, initializes the storage file path and ensures the storage file exists.
     *
     * @param context Servlet context, used to obtain the real path of the storage file
     */
    public ApplicationStorage(ServletContext context) {
        this.storageFile = new File(context.getRealPath(STORAGE_PATH));
        this.servletContext = context;
        ensureStorageExists();
    }

    /**
     * Ensures the storage file and its parent directory exist; creates them if they do not.
     * Writes an empty JSON array on first creation.
     */
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

    /**
     * Loads all application records from the file.
     *
     * @return the list of application records, or an empty list if the file is empty
     */
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

    /**
     * Checks and updates expired application records.
     * <p>Automatically marks the status of applications that have not been processed for more than 7 days
     * (other than Accepted, Rejected, Expired) as Expired.</p>
     *
     * @param applications the list of application records to check
     */
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

    /**
     * Writes all application records to the storage file.
     *
     * @param list the list of application records to save
     */
    private void saveAll(List<Application> list) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(storageFile, list);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write applications storage", e);
        }
    }

    /**
     * Finds application records by TA email.
     *
     * @param email the TA's email address
     * @return the list of matching application records, or an empty list if the email is null
     */
    public List<Application> findByTaEmail(String email) {
        if (email == null) {
            return new ArrayList<>();
        }
        String normalized = email.toLowerCase();
        return loadAll().stream()
                .filter(app -> normalized.equals(app.getTaEmail()))
                .collect(Collectors.toList());
    }

    /**
     * Saves a new application record and logs an audit entry.
     *
     * @param application the application object to save
     */
    public void save(Application application) {
        List<Application> apps = loadAll();
        apps.add(application);
        saveAll(apps);
        try {
            com.bupt.ta.storage.AuditLogStorage audit = new com.bupt.ta.storage.AuditLogStorage(servletContext);
            audit.add(new com.bupt.ta.model.AuditLogEntry(application.getTaEmail(), "Application Create", application.getId(), "Applied to: " + application.getPositionTitle()));
        } catch (Exception ignored) {}
    }

    /**
     * Checks whether the specified TA has already applied for the specified position.
     *
     * @param taEmail    the TA's email address
     * @param positionId the position ID
     * @return true if an application already exists; false otherwise
     */
    public boolean hasApplied(String taEmail, String positionId) {
        if (taEmail == null || positionId == null) {
            return false;
        }
        String normalizedEmail = taEmail.toLowerCase();
        return loadAll().stream()
                .anyMatch(app -> normalizedEmail.equals(app.getTaEmail()) && positionId.equals(app.getPositionId()));
    }

    /**
     * Creates a new application record with the default status "Pending".
     *
     * @param taEmail       the TA's email address
     * @param positionId    the position ID
     * @param positionTitle the position title
     * @return the created application object
     */
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
     * Updates the status of the specified application (operator defaults to "system").
     *
     * @param applicationId the application ID
     * @param status        the new application status
     */
    public void updateStatus(String applicationId, Application.Status status) {
        updateStatus(applicationId, status, "system");
    }

    /**
     * Updates the status of the specified application and records the operator.
     *
     * @param applicationId the application ID
     * @param status        the new application status
     * @param operator      the operator identifier
     */
    public void updateStatus(String applicationId, Application.Status status, String operator) {
        updateStatus(Collections.singletonList(applicationId), status, operator);
    }

    /**
     * Batch updates the status of multiple applications and logs audit entries.
     *
     * @param applicationIds the list of application IDs to update
     * @param status         the new application status
     * @param operator       the operator identifier
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

    /**
     * Updates the assigned workload of the specified application and logs an audit entry.
     *
     * @param applicationId          the application ID
     * @param assignedWorkloadHours  the assigned workload (in hours); null indicates clearing
     * @param operator               the operator identifier
     */
    public void updateAssignedWorkload(String applicationId, Integer assignedWorkloadHours, String operator) {
        if (applicationId == null || applicationId.isBlank()) {
            return;
        }

        List<Application> apps = loadAll();
        boolean updated = false;
        for (Application app : apps) {
            if (applicationId.equals(app.getId())) {
                app.setAssignedWorkloadHours(assignedWorkloadHours);
                updated = true;
            }
        }
        if (!updated) {
            return;
        }
        saveAll(apps);

        try {
            com.bupt.ta.storage.AuditLogStorage audit = new com.bupt.ta.storage.AuditLogStorage(servletContext);
            String operatorId = operator != null ? operator : "system";
            audit.add(new com.bupt.ta.model.AuditLogEntry(operatorId, "Workload Update", applicationId,
                    "Assigned workload updated to: " + (assignedWorkloadHours == null ? "[clear]" : assignedWorkloadHours + " hours")));
        } catch (Exception ignored) {}
    }

    /**
     * Retrieves all application records.
     *
     * @return the list of all applications
     */
    public List<Application> findAll() {
        return loadAll();
    }
}
