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

    /**
     * Constructs a {@code ProfileStorage} instance and ensures the underlying
     * JSON storage file exists.
     *
     * @param servletContext the ServletContext used to resolve the real path to the storage file
     */
    public ProfileStorage(ServletContext servletContext) {
        this.storageFile = new File(servletContext.getRealPath(PROFILE_FILE_NAME));
        ensureStorageExists();
    }

    /**
     * Ensures that the directory and storage file exist, creating them if necessary.
     * If the file does not exist, it is initialised with an empty JSON object.
     *
     * @throws IllegalStateException if the directory or file cannot be created
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
     * Loads all TA profiles from the JSON storage file. The map keys are email
     * addresses (lowercase) and the values are the corresponding profiles.
     *
     * @return a mutable map of email-to-profile entries (never {@code null})
     * @throws IllegalStateException if the storage file cannot be read
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
     * Persists the given map of profiles to the JSON storage file with
     * pretty-printing.
     *
     * @param profiles the profiles to write (must not be {@code null})
     * @throws IllegalStateException if the file cannot be written
     */
    private void saveAll(Map<String, TAProfile> profiles) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(storageFile, profiles);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write profile storage", e);
        }
    }

    /**
     * Loads the TA profile for the given email address. The lookup is
     * case-insensitive.
     *
     * @param email the email address to look up; if {@code null}, returns {@code null}
     * @return the matching profile, or {@code null} if not found
     */
    public TAProfile load(String email) {
        if (email == null) {
            return null;
        }
        Map<String, TAProfile> all = loadAll();
        return all.get(email.toLowerCase());
    }

    /**
     * Saves or updates a TA profile in persistent storage. If the profile or its
     * email is {@code null}, this operation is a no-op.
     *
     * @param profile the profile to persist; if {@code null} or has a {@code null}
     *                email, this method does nothing
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
