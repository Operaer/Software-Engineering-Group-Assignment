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

/**
 * JSON file storage manager for user data.
 * <p>Responsible for persisting user accounts in the TA recruitment system, supporting user
 * CRUD operations, authentication, and admin count statistics.</p>
 */
public class UserStorage {
    private static final String USERS_FILE = AppConfig.USERS_FILE;

    private final ObjectMapper objectMapper;
    private final File usersFile;
    private final javax.servlet.ServletContext servletContext;

    /**
     * Constructs a UserStorage instance, initializes the storage file path and ensures the parent
     * directory exists.
     *
     * @param context Servlet context, used to obtain the real path of the storage file
     */
    public UserStorage(ServletContext context) {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.usersFile = new File(context.getRealPath(USERS_FILE));
        this.servletContext = context;

        File parent = usersFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    /**
     * Loads all user data from the file (internal method).
     * <p>Automatically repairs the email and username fields of user objects to ensure data integrity.</p>
     *
     * @return a mapping from email to User object
     * @throws IOException if reading the file fails
     */
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

    /**
     * Writes all user data to the storage file (internal method).
     *
     * @param users the mapping from email to User object
     * @throws IOException if writing the file fails
     */
    private void saveUsersInternal(Map<String, User> users) throws IOException {
        objectMapper.writeValue(usersFile, users);
    }

    /**
     * Retrieves all users.
     *
     * @return a mapping from email to User object
     * @throws IOException if reading the file fails
     */
    public Map<String, User> getAllUsers() throws IOException {
        return loadUsersInternal();
    }

    /**
     * Finds a user by email.
     *
     * @param email the user's email address
     * @return the matching User object, or null if not found
     * @throws IOException if reading the file fails
     */
    public User findByEmail(String email) throws IOException {
        if (email == null || email.isBlank()) {
            return null;
        }
        return loadUsersInternal().get(email.trim().toLowerCase());
    }

    /**
     * Checks whether the specified email is already registered.
     *
     * @param email the email address to check
     * @return true if the email already exists; false otherwise
     * @throws IOException if reading the file fails
     */
    public boolean emailExists(String email) throws IOException {
        if (email == null || email.isBlank()) {
            return false;
        }
        return loadUsersInternal().containsKey(email.trim().toLowerCase());
    }

    /**
     * Checks whether the specified username is already in use.
     *
     * @param username the username to check
     * @return true if the username already exists; false otherwise
     * @throws IOException if reading the file fails
     */
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

    /**
     * Authenticates user login credentials.
     *
     * @param email    the user's email
     * @param password the user's password
     * @return the User object if authentication succeeds; null otherwise
     * @throws IOException if reading the file fails
     */
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

    /**
     * Creates a new user and logs an audit entry.
     * <p>Checks whether the email and username are already taken; throws an exception if they are.</p>
     *
     * @param user the user object to create
     * @throws IOException              if reading or writing the file fails
     * @throws IllegalArgumentException if the email/username already exists or the email is empty
     */
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
        try {
            com.bupt.ta.storage.AuditLogStorage audit = new com.bupt.ta.storage.AuditLogStorage(servletContext);
            audit.add(new com.bupt.ta.model.AuditLogEntry(user.getEmail(), "Account Create", user.getEmail(), "Created new user account."));
        } catch (Exception ignored) {}
    }

    /**
     * Updates user information and logs an audit entry.
     *
     * @param user the updated user object
     * @throws IOException              if reading or writing the file fails
     * @throws IllegalArgumentException if the user email is empty
     */
    public void updateUser(User user) throws IOException {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("User email cannot be empty.");
        }

        Map<String, User> users = loadUsersInternal();
        String key = user.getEmail().trim().toLowerCase();
        user.setEmail(key);
        users.put(key, user);
        saveUsersInternal(users);
        try {
            com.bupt.ta.storage.AuditLogStorage audit = new com.bupt.ta.storage.AuditLogStorage(servletContext);
            audit.add(new com.bupt.ta.model.AuditLogEntry(user.getEmail(), "Account Update", user.getEmail(), "Updated user account."));
        } catch (Exception ignored) {}
    }

    /**
     * Deletes the specified user and logs an audit entry.
     *
     * @param email the email address of the user to delete
     * @throws IOException if reading or writing the file fails
     */
    public void deleteUser(String email) throws IOException {
        if (email == null || email.isBlank()) {
            return;
        }

        Map<String, User> users = loadUsersInternal();
        users.remove(email.trim().toLowerCase());
        saveUsersInternal(users);
        try {
            com.bupt.ta.storage.AuditLogStorage audit = new com.bupt.ta.storage.AuditLogStorage(servletContext);
            audit.add(new com.bupt.ta.model.AuditLogEntry(email, "Account Delete", email, "Deleted user account."));
        } catch (Exception ignored) {}
    }

    /**
     * Counts the total number of administrators (ADMIN role) in the system.
     *
     * @return the number of admin users
     * @throws IOException if reading the file fails
     */
    public int countAdmins() throws IOException {
        int count = 0;
        for (User user : loadUsersInternal().values()) {
            if (user != null && user.getRole() == User.Role.ADMIN) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts the number of active administrators (ADMIN role) in the system.
     *
     * @return the number of active admins
     * @throws IOException if reading the file fails
     */
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
