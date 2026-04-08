package com.bupt.ta.security;

import com.bupt.ta.model.User;

public class PermissionChecker {

    public static boolean hasPermission(User user, User.Role requiredRole) {
        return user != null
                && user.isActive()
                && user.getRole() != null
                && user.hasPermission(requiredRole);
    }

    public static boolean canAccessResource(User user, String resource) {
        if (user == null || !user.isActive() || user.getRole() == null || resource == null) {
            return false;
        }

        if (resource.startsWith("/secure/admin/")) {
            return user.getRole() == User.Role.ADMIN;
        }

        if (resource.startsWith("/secure/mo/")) {
            return user.getRole() == User.Role.MO || user.getRole() == User.Role.ADMIN;
        }

        if (resource.startsWith("/secure/ta/")) {
            return user.getRole() == User.Role.TA
                    || user.getRole() == User.Role.MO
                    || user.getRole() == User.Role.ADMIN;
        }

        if ("/dashboard".equals(resource)) {
            return true;
        }

        return false;
    }

    public static boolean canManageUsers(User user) {
        return hasPermission(user, User.Role.ADMIN);
    }

    public static boolean canModifyApplicationStatus(User user) {
        return hasPermission(user, User.Role.MO);
    }

    public static boolean canPostPosition(User user) {
        return hasPermission(user, User.Role.MO);
    }

    public static boolean canViewAuditLogs(User user) {
        return hasPermission(user, User.Role.ADMIN);
    }
}