package com.bupt.ta.storage;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.Application;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JSON-backed storage for TA applications.
 */
public class ApplicationStorage {
    private static final String STORAGE_PATH = AppConfig.APPLICATIONS_FILE;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final File storageFile;

    public ApplicationStorage(ServletContext context) {
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
                mapper.writeValue(storageFile, new ArrayList<>());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize application storage", e);
        }
    }

    private List<Application> loadAll() {
        try {
            TypeFactory factory = mapper.getTypeFactory();
            CollectionType listType = factory.constructCollectionType(ArrayList.class, Application.class);
            List<Application> list = mapper.readValue(storageFile, listType);
            return list != null ? list : new ArrayList<>();
        } catch (JsonProcessingException e) {
            // Recover from corrupted JSON by resetting storage to an empty list.
            saveAll(new ArrayList<>());
            return new ArrayList<>();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read applications storage", e);
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

    public void updateStatus(String applicationId, Application.Status status) {
        List<Application> apps = loadAll();
        for (Application app : apps) {
            if (app.getId().equals(applicationId)) {
                app.setStatus(status.name());
                break;
            }
        }
        saveAll(apps);
    }

    public List<Application> findAll() {
        return loadAll();
    }
}
