package com.bupt.ta.model;

import java.io.Serializable;

public class User implements Serializable {
    private String email;
    private String password; // 添加密码字段
    private Role role;
    private boolean active; // 添加状态字段，true为启用，false为停用

    public User() {
    }

    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true; // 默认启用
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
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

    public void setActive(boolean active) {
        this.active = active;
    }

    // 检查用户是否有足够权限（基于层次）
    public boolean hasPermission(Role requiredRole) {
        return this.role.ordinal() >= requiredRole.ordinal();
    }

    public enum Role {
        TA(1),
        MO(2),
        ADMIN(3);

        private final int level;

        Role(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
}
