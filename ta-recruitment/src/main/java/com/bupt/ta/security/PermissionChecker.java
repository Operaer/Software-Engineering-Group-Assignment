package com.bupt.ta.security;

import com.bupt.ta.model.User;

/**
 * Permission checker for the TA recruitment system.
 * <p>Provides role-based and resource-based permission checking, used to control access
 * to management resources for different roles within the system.</p>
 */
public class PermissionChecker {

    /**
     * Checks whether the user has the permission corresponding to the specified role.
     *
     * @param user         the user object to check
     * @param requiredRole the required role
     * @return true if the user is not null, active, and has the specified role permission; false otherwise
     */
    public static boolean hasPermission(User user, User.Role requiredRole) {
        return user != null
                && user.isActive()
                && user.getRole() != null
                && user.hasPermission(requiredRole);
    }

    /**
     * Checks whether the user has permission to access the specified resource path.
     * <p>The required role is determined by the resource path prefix:</p>
     * <ul>
     *   <li>/secure/admin/ paths are accessible only by ADMIN</li>
     *   <li>/secure/mo/ paths require MO or ADMIN role</li>
     *   <li>/secure/ta/ paths require TA, MO, or ADMIN role</li>
     *   <li>/dashboard is accessible to any active user</li>
     * </ul>
     *
     * @param user     the user object to check
     * @param resource the requested resource path
     * @return true if the user has permission to access the resource; false otherwise
     */
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

    /**
     * Checks whether the user has user management permission (ADMIN role only).
     *
     * @param user the user object to check
     * @return true if the user has the ADMIN role; false otherwise
     */
    public static boolean canManageUsers(User user) {
        return hasPermission(user, User.Role.ADMIN);
    }

    /**
     * Checks whether the user has permission to modify application status (requires MO role or above).
     *
     * @param user the user object to check
     * @return true if the user has MO role or above; false otherwise
     */
    public static boolean canModifyApplicationStatus(User user) {
        return hasPermission(user, User.Role.MO);
    }

    /**
     * Checks whether the user has permission to post positions (requires MO role or above).
     *
     * @param user the user object to check
     * @return true if the user has MO role or above; false otherwise
     */
    public static boolean canPostPosition(User user) {
        return hasPermission(user, User.Role.MO);
    }

    /**
     * Checks whether the user has permission to view audit logs (ADMIN role only).
     *
     * @param user the user object to check
     * @return true if the user has the ADMIN role; false otherwise
     */
    public static boolean canViewAuditLogs(User user) {
        return hasPermission(user, User.Role.ADMIN);
    }
}
