package com.bupt.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {
    private String username;
    private String email;
    private String password;
    private Role role;
    private boolean active;

    public User() {
    }

    public User(String username, String email, String password, Role role, boolean active) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
    }

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

    public String getUsername() {
        if (username == null || username.isBlank()) {
            username = buildDefaultUsername(email);
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        if (this.username == null || this.username.isBlank()) {
            this.username = buildDefaultUsername(email);
        }
    }

    public String getPassword() {
        return password;
    }

    public boolean passwordMatches(String rawPassword) {
        return password != null && password.equals(rawPassword);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    @JsonIgnore
    public String getStatusText() {
        return active ? "Active" : "Disabled";
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean hasPermission(Role requiredRole) {
        return this.role != null && requiredRole != null && this.role.ordinal() >= requiredRole.ordinal();
    }

    public enum Role {
        TA,
        MO,
        ADMIN
    }
}