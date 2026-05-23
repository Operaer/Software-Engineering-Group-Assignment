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
 * JSON file storage manager for TA profiles.
 * <p>Stores TA personal information in key-value (email -&gt; TAProfile) JSON format,
 * supporting profile loading, saving, and lookup by email.</p>
 */
public class ProfileStorage {
    private static final String PROFILE_FILE_NAME = AppConfig.PROFILES_FILE;
    private final ObjectMapper mapper = new ObjectMapper();

    private final File storageFile;

    /**
     * Constructs a ProfileStorage instance, initializes the storage file and ensures the file exists.
     *
     * @param servletContext Servlet context, used to obtain the real path of the storage file
     */
    public ProfileStorage(ServletContext servletContext) {
        this.storageFile = new File(servletContext.getRealPath(PROFILE_FILE_NAME));
        ensureStorageExists();
    }

    /**
     * Ensures the storage file and its parent directory exist; creates them if they do not.
     * Writes an empty JSON object on first creation.
     */
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

    /**
     * Loads all TA profiles from the file.
     *
     * @return a mapping from email to TAProfile
     */
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

    /**
     * Writes all TA profiles to the storage file.
     *
     * @param profiles the mapping from email to TAProfile
     */
    private void saveAll(Map<String, TAProfile> profiles) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(storageFile, profiles);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write profile storage", e);
        }
    }

    /**
     * Loads a TA profile by email.
     *
     * @param email the TA's email address
     * @return the corresponding TAProfile object, or null if not found
     */
    public TAProfile load(String email) {
        if (email == null) {
            return null;
        }
        Map<String, TAProfile> all = loadAll();
        return all.get(email.toLowerCase());
    }

    /**
     * Saves or updates a TA profile (keyed by email).
     *
     * @param profile the TAProfile object to save
     */
    public void save(TAProfile profile) {
        if (profile == null || profile.getEmail() == null) {
            return;
        }
        Map<String, TAProfile> all = loadAll();
        all.put(profile.getEmail().toLowerCase(), profile);
        saveAll(all);
    }
}
