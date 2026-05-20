package com.bupt.ta.storage;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.TAProfile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple JSON-based storage for TA profiles.
 */
public class ProfileStorage {
    private static final String PROFILE_FILE_NAME = AppConfig.PROFILES_FILE;
    private final ObjectMapper mapper = new ObjectMapper();

    private final File storageFile;

    public ProfileStorage(ServletContext servletContext) {
        this.storageFile = new File(servletContext.getRealPath(PROFILE_FILE_NAME));
        ensureStorageExists();
    }

    private void ensureStorageExists() {
        try {
            File parent = storageFile.getParentFile();
            if (!parent.exists()) {
                Files.createDirectories(parent.toPath());
            }
            if (!storageFile.exists()) {
                mapper.writeValue(storageFile, Collections.emptyMap());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize profile storage", e);
        }
    }

    private Map<String, TAProfile> loadAll() {
        try {
            TypeFactory typeFactory = mapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, TAProfile.class);
            Map<String, TAProfile> profiles = mapper.readValue(storageFile, mapType);
            return profiles != null ? profiles : new HashMap<>();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read profile storage", e);
        }
    }

    private void saveAll(Map<String, TAProfile> profiles) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(storageFile, profiles);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write profile storage", e);
        }
    }

    public TAProfile load(String email) {
        if (email == null) {
            return null;
        }
        Map<String, TAProfile> all = loadAll();
        return all.get(email.toLowerCase());
    }

    public void save(TAProfile profile) {
        if (profile == null || profile.getEmail() == null) {
            return;
        }
        Map<String, TAProfile> all = loadAll();
        all.put(profile.getEmail().toLowerCase(), profile);
        saveAll(all);
    }
}
