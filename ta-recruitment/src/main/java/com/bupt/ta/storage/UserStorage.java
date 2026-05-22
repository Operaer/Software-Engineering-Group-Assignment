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
 * JSON-backed storage for user accounts.
 *
 * <p>This class manages persisted user records in the embedded file-based
 * storage used by the TA recruitment system. Users are stored as a map keyed
 * by lowercase email address.</p>
 *
 * @author Operaer
 * @date 2026-05-17
 */
public class UserStorage {
    private static final String USERS_FILE = AppConfig.USERS_FILE;

    private final ObjectMapper objectMapper;
    private final File usersFile;
    private final javax.servlet.ServletContext servletContext;

    /**
     * Constructs a {@code UserStorage} instance and ensures the parent directory
     * for the storage file exists.
     *
     * @param context the ServletContext used to resolve the real path to the storage file
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
     * Loads all users from the JSON storage file. Each entry is backed by a
     * {@link User} object; missing email or username fields are automatically
     * populated from the map key or the email prefix respectively.
     *
     * @return a mutable map of lowercase-email to user entries (never {@code null})
     * @throws IOException if the storage file cannot be read
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
     * Persists the given map of users to the JSON storage file.
     *
     * @param users the users to write (must not be {@code null})
     * @throws IOException if the file cannot be written
     */
    private void saveUsersInternal(Map<String, User> users) throws IOException {
        objectMapper.writeValue(usersFile, users);
    }

    /**
     * Returns all registered users as a map keyed by lowercase email address.
     *
     * @return a map of email-to-user entries (never {@code null})
     * @throws IOException if the storage file cannot be read
     */
    public Map<String, User> getAllUsers() throws IOException {
        return loadUsersInternal();
    }

    /**
     * Finds a user by their email address. The lookup is case-insensitive.
     *
     * @param email the email to search for; if {@code null} or blank, returns {@code null}
     * @return the matching user, or {@code null} if not found
     * @throws IOException if the storage file cannot be read
     */
    public User findByEmail(String email) throws IOException {
        if (email == null || email.isBlank()) {
            return null;
        }
        return loadUsersInternal().get(email.trim().toLowerCase());
    }

    /**
     * Checks whether an account with the given email address already exists.
     * The lookup is case-insensitive.
     *
     * @param email the email to check; if {@code null} or blank, returns {@code false}
     * @return {@code true} if a user with that email exists, {@code false} otherwise
     * @throws IOException if the storage file cannot be read
     */
    public boolean emailExists(String email) throws IOException {
        if (email == null || email.isBlank()) {
            return false;
        }
        return loadUsersInternal().containsKey(email.trim().toLowerCase());
    }

    /**
     * Checks whether a user with the given username already exists.
     * The comparison is case-insensitive.
     *
     * @param username the username to check; if {@code null} or blank, returns {@code false}
     * @return {@code true} if a user with that username exists, {@code false} otherwise
     * @throws IOException if the storage file cannot be read
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
     * Authenticates a user by email and password. Only active users can
     * successfully authenticate.
     *
     * @param email    the user's email address
     * @param password the user's password
     * @return the authenticated {@link User} if credentials are valid and the
     *         account is active, or {@code null} otherwise
     * @throws IOException if the storage file cannot be read
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
     * Creates a new user account and persists it. Duplicate email addresses and
     * duplicate (non-blank) usernames are rejected. An audit-log entry is recorded
     * on a best-effort basis.
     *
     * @param user the user to create (must not be {@code null} and must have a
     *             non-blank email)
     * @throws IllegalArgumentException if the email or username already exists,
     *                                  or if the email is empty
     * @throws IOException if the storage file cannot be written
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
     * Updates an existing user account. If the user does not already exist, it
     * is created. An audit-log entry is recorded on a best-effort basis.
     *
     * @param user the user with updated fields (must not be {@code null} and must
     *             have a non-blank email)
     * @throws IllegalArgumentException if the email is empty
     * @throws IOException if the storage file cannot be written
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
     * Deletes a user account by email. An audit-log entry is recorded on a
     * best-effort basis.
     *
     * @param email the email of the user to delete; if {@code null} or blank,
     *              this method does nothing
     * @throws IOException if the storage file cannot be written
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
     * Counts the total number of admin user accounts.
     *
     * @return the number of users with the {@link User.Role#ADMIN} role
     * @throws IOException if the storage file cannot be read
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
     * Counts the number of active admin user accounts (admins whose account is
     * enabled).
     *
     * @return the number of users with the {@link User.Role#ADMIN} role and
     *         {@code active = true}
     * @throws IOException if the storage file cannot be read
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