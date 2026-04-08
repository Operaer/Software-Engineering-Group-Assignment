package com.bupt.ta.storage;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserStorage {
    private static final String USERS_FILE = AppConfig.USERS_FILE;

    private final ObjectMapper objectMapper;
    private final File usersFile;

    public UserStorage(ServletContext context) {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.usersFile = new File(context.getRealPath(USERS_FILE));

        File parent = usersFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    private Map<String, User> loadUsersInternal() throws IOException {
        if (!usersFile.exists() || usersFile.length() == 0) {
            return new LinkedHashMap<>();
        }

        Map<String, User> users = objectMapper.readValue(
                usersFile,
                new TypeReference<LinkedHashMap<String, User>>() {}
        );

        if (users == null) {
            users = new LinkedHashMap<>();
        }

        for (Map.Entry<String, User> entry : users.entrySet()) {
            User user = entry.getValue();
            if (user != null) {
                if (user.getEmail() == null || user.getEmail().isBlank()) {
                    user.setEmail(entry.getKey());
                }
                if (user.getUsername() == null || user.getUsername().isBlank()) {
                    String email = user.getEmail();
                    int at = email.indexOf('@');
                    user.setUsername(at > 0 ? email.substring(0, at) : email);
                }
            }
        }

        return users;
    }

    private void saveUsersInternal(Map<String, User> users) throws IOException {
        objectMapper.writeValue(usersFile, users);
    }

    public Map<String, User> getAllUsers() throws IOException {
        return loadUsersInternal();
    }

    public User findByEmail(String email) throws IOException {
        if (email == null || email.isBlank()) {
            return null;
        }
        return loadUsersInternal().get(email.trim().toLowerCase());
    }

    public boolean emailExists(String email) throws IOException {
        if (email == null || email.isBlank()) {
            return false;
        }
        return loadUsersInternal().containsKey(email.trim().toLowerCase());
    }

    public boolean usernameExists(String username) throws IOException {
        if (username == null || username.isBlank()) {
            return false;
        }

        String normalized = username.trim().toLowerCase();
        for (User user : loadUsersInternal().values()) {
            if (user != null && user.getUsername() != null
                    && user.getUsername().trim().toLowerCase().equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    public User authenticate(String email, String password) throws IOException {
        if (email == null || password == null) {
            return null;
        }

        User user = findByEmail(email);
        if (user == null) {
            return null;
        }

        if (!user.isActive()) {
            return null;
        }

        return user.passwordMatches(password) ? user : null;
    }

    public void createUser(User user) throws IOException {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("User email cannot be empty.");
        }

        Map<String, User> users = loadUsersInternal();
        String key = user.getEmail().trim().toLowerCase();

        if (users.containsKey(key)) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }

        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            String normalizedUsername = user.getUsername().trim().toLowerCase();
            for (User existingUser : users.values()) {
                if (existingUser != null && existingUser.getUsername() != null
                        && existingUser.getUsername().trim().toLowerCase().equals(normalizedUsername)) {
                    throw new IllegalArgumentException("A user with this username already exists.");
                }
            }
        }

        user.setEmail(key);
        users.put(key, user);
        saveUsersInternal(users);
    }

    public void updateUser(User user) throws IOException {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("User email cannot be empty.");
        }

        Map<String, User> users = loadUsersInternal();
        String key = user.getEmail().trim().toLowerCase();
        user.setEmail(key);
        users.put(key, user);
        saveUsersInternal(users);
    }

    public void deleteUser(String email) throws IOException {
        if (email == null || email.isBlank()) {
            return;
        }

        Map<String, User> users = loadUsersInternal();
        users.remove(email.trim().toLowerCase());
        saveUsersInternal(users);
    }

    public int countAdmins() throws IOException {
        int count = 0;
        for (User user : loadUsersInternal().values()) {
            if (user != null && user.getRole() == User.Role.ADMIN) {
                count++;
            }
        }
        return count;
    }

    public int countActiveAdmins() throws IOException {
        int count = 0;
        for (User user : loadUsersInternal().values()) {
            if (user != null && user.getRole() == User.Role.ADMIN && user.isActive()) {
                count++;
            }
        }
        return count;
    }
}