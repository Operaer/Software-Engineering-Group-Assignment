package com.bupt.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Represents a user account in the TA recruitment system.
 *
 * <p>Each user has a username, email, password, role (TA, MO, or ADMIN),
 * and an active status flag. The role-based permission model uses ordinal
 * comparison to determine access rights. Passwords are stored as plain text
 * and compared via the {@link #passwordMatches(String)} method.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {
    private String username;
    private String email;
    private String password;
    private Role role;
    private boolean active;

    /**
     * Constructs an empty User for deserialization.
     */
    public User() {
    }

    /**
     * Constructs a User with all fields specified.
     *
     * @param username the username
     * @param email    the email address
     * @param password the password
     * @param role     the user role
     * @param active   whether the account is active
     */
    public User(String username, String email, String password, Role role, boolean active) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    /**
     * Constructs a User from email, password, and role, deriving the username
     * from the email prefix and defaulting the account to active.
     *
     * @param email    the email address
     * @param password the password
     * @param role     the user role
     */
    public User(String email, String password, Role role) {
        this.username = buildDefaultUsername(email);
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true;
    }

    private static String buildDefaultUsername(String email) {
        if (email == null || email.isBlank()) {
            return "user";
        }
        int at = email.indexOf('@');
        if (at > 0) {
            return email.substring(0, at);
        }
        return email;
    }

    /**
     * Returns the username, deriving it from the email prefix if not explicitly set.
     *
     * @return the username
     */
    public String getUsername() {
        if (username == null || username.isBlank()) {
            username = buildDefaultUsername(email);
        }
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address, also deriving the username from the email prefix
     * if the username has not been explicitly set.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
        if (this.username == null || this.username.isBlank()) {
            this.username = buildDefaultUsername(email);
        }
    }

    /**
     * Returns the password.
     *
     * @return the password string
     */
    public String getPassword() {
        return password;
    }

    /**
     * Checks whether the given raw password matches the stored password.
     *
     * @param rawPassword the raw password to check
     * @return {@code true} if the passwords match, {@code false} otherwise
     */
    public boolean passwordMatches(String rawPassword) {
        return password != null && password.equals(rawPassword);
    }

    /**
     * Sets the password.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the user's role.
     *
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role the role to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Returns whether the account is active.
     *
     * @return {@code true} if the account is active, {@code false} otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns a human-readable status text based on the active flag.
     *
     * @return "Active" if the account is active, "Disabled" otherwise
     */
    @JsonIgnore
    public String getStatusText() {
        return active ? "Active" : "Disabled";
    }

    /**
     * Sets whether the account is active.
     *
     * @param active {@code true} to activate the account, {@code false} to disable it
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Checks whether this user has at least the given role level, based on
     * ordinal comparison (TA &lt; MO &lt; ADMIN).
     *
     * @param requiredRole the minimum required role
     * @return {@code true} if this user's role meets or exceeds the required level
     */
    public boolean hasPermission(Role requiredRole) {
        return this.role != null && requiredRole != null && this.role.ordinal() >= requiredRole.ordinal();
    }

    /**
     * Represents the user roles in the TA recruitment system, ordered by
     * increasing privilege: TA, MO, ADMIN.
     */
    public enum Role {
        TA,
        MO,
        ADMIN
    }
}