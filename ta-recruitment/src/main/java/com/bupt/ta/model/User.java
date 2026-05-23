package com.bupt.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * User model in the TA recruitment system.
 *
 * <p>The user contains login credentials (username, email, password), role, and
 * account activation status. The username is auto-generated from the email address
 * when not explicitly set. The ordinal values of the TA / MO / ADMIN role enum
 * support permission level comparison.</p>
 *
 * @author Operaer
 * @since 2026-05-17
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {
    private String username;
    private String email;
    private String password;
    private Role role;
    private boolean active;

    /** No-arg constructor */
    public User() {
    }

    /**
     * All-args constructor.
     *
     * @param username Username
     * @param email    Email
     * @param password Password
     * @param role     Role
     * @param active   Whether the account is active
     */
    public User(String username, String email, String password, Role role, boolean active) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    /**
     * Simplified constructor, auto-generates a default username from the email
     * and activates the account by default.
     *
     * @param email    Email
     * @param password Password
     * @param role     Role
     */
    public User(String email, String password, Role role) {
        this.username = buildDefaultUsername(email);
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true;
    }

    /**
     * Generates a default username from the email address (takes the part before @).
     *
     * @param email Email address
     * @return Generated username
     */
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
     * Gets the username. Auto-generates a default username from the email if not set.
     *
     * @return Username
     */
    public String getUsername() {
        if (username == null || username.isBlank()) {
            username = buildDefaultUsername(email);
        }
        return username;
    }

    /** Sets the username */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the email. Auto-generates a default username if the username is empty
     * when the email is set.
     *
     * @return Email address
     */
    public String getEmail() {
        return email;
    }

    /** Sets the email, auto-generates a default username if the username is empty */
    public void setEmail(String email) {
        this.email = email;
        if (this.username == null || this.username.isBlank()) {
            this.username = buildDefaultUsername(email);
        }
    }

    /** Gets the password */
    public String getPassword() {
        return password;
    }

    /**
     * Checks whether the raw password matches.
     *
     * @param rawPassword The plaintext password to verify
     * @return true if matched
     */
    public boolean passwordMatches(String rawPassword) {
        return password != null && password.equals(rawPassword);
    }

    /** Sets the password */
    public void setPassword(String password) {
        this.password = password;
    }

    /** Gets the role */
    public Role getRole() {
        return role;
    }

    /** Sets the role */
    public void setRole(Role role) {
        this.role = role;
    }

    /** Checks whether the account is active */
    public boolean isActive() {
        return active;
    }

    /**
     * Gets the textual description of the account status.
     *
     * @return "Active" or "Disabled"
     */
    @JsonIgnore
    public String getStatusText() {
        return active ? "Active" : "Disabled";
    }

    /** Sets the account activation status */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Checks whether the current user has the permission of the specified role.
     * Comparison is based on enum ordinal values; higher ordinal means higher permission.
     *
     * @param requiredRole The required role
     * @return true if the current user's role is not lower than the required role
     */
    public boolean hasPermission(Role requiredRole) {
        return this.role != null && requiredRole != null && this.role.ordinal() >= requiredRole.ordinal();
    }

    /**
     * User role enumeration.
     *
     * <p>TA (Teaching Assistant) has the lowest permission, MO (Module Organiser)
     * is in the middle, and ADMIN has the highest permission. Permission comparison
     * is based on ordinal values.</p>
     */
    public enum Role {
        TA,
        MO,
        ADMIN
    }
}
