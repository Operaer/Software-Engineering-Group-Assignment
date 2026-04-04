package com.bupt.ta.storage;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.TAProfile;
import javax.servlet.ServletContext;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Simple text-based storage for TA profiles.
 */
public class ProfileStorage {
    private static final String PROFILE_FILE_NAME = AppConfig.PROFILES_FILE;
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
                storageFile.createNewFile();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize profile storage", e);
        }
    }

    private Map<String, TAProfile> loadAll() {
        Map<String, TAProfile> profiles = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1); // -1 to include empty fields
                if (parts.length >= 7) {
                    TAProfile profile = new TAProfile();
                    profile.setEmail(parts[0]);
                    profile.setName(parts[1]);
                    profile.setStudentId(parts[2]);
                    profile.setMajor(parts[3]);
                    profile.setPhone(parts[4]);
                    if (!parts[5].isEmpty()) {
                        String[] skills = parts[5].split(";");
                        profile.getSkills().addAll(Arrays.asList(skills));
                    }
                    profile.setResumeFileName(parts[6]);
                    profiles.put(profile.getEmail().toLowerCase(), profile);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read profile storage", e);
        }
        return profiles;
    }

    private void saveAll(Map<String, TAProfile> profiles) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(storageFile))) {
            for (TAProfile profile : profiles.values()) {
                String skills = String.join(";", profile.getSkills());
                writer.println(profile.getEmail() + "," +
                        (profile.getName() != null ? profile.getName() : "") + "," +
                        (profile.getStudentId() != null ? profile.getStudentId() : "") + "," +
                        (profile.getMajor() != null ? profile.getMajor() : "") + "," +
                        (profile.getPhone() != null ? profile.getPhone() : "") + "," +
                        skills + "," +
                        (profile.getResumeFileName() != null ? profile.getResumeFileName() : ""));
            }
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
