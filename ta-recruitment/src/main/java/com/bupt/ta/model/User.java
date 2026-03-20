package com.bupt.ta.model;

import java.io.Serializable;

public class User implements Serializable {
    private String email;
    private Role role;

    public User() {
    }

    public User(String email, Role role) {
        this.email = email;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public enum Role {
        TA,
        MO,
        ADMIN
    }
}
