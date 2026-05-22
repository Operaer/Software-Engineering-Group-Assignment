package com.bupt.ta.security;

import com.bupt.ta.model.User;

/**
 * Utility class for role-based permission checks within the TA recruitment system.
 * Provides static methods to verify whether a user holds the required role
 * or can access a specific secured resource path.
 */
public class PermissionChecker {

    /**
     * Checks whether the given user has the specified role permission.
     * The user must be non-null, active, and have a role that meets or
     * exceeds the required role level.
     *
     * @param user        the user to check; may be null
     * @param requiredRole the minimum role required
     * @return {@code true} if the user is active and has the required permission
     */
    public static boolean hasPermission(User user, User.Role requiredRole) {
        return user != null
                && user.isActive()
                && user.getRole() != null
                && user.hasPermission(requiredRole);
    }

    /**
     * Determines whether an active user can access a given resource path.
     * Access rules are based on URL prefixes:
     * <ul>
     *   <li>{@code /secure/admin/*} -- ADMIN only</li>
     *   <li>{@code /secure/mo/*} -- MO or ADMIN</li>
     *   <li>{@code /secure/ta/*} -- TA, MO, or ADMIN</li>
     *   <li>{@code /dashboard} -- any authenticated active user</li>
     * </ul>
     *
     * @param user     the user to check; may be null
     * @param resource the resource path being requested; may be null
     * @return {@code true} if access is granted
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
     * Returns whether the user can manage other users (ADMIN role required).
     *
     * @param user the user to check
     * @return {@code true} if the user is an active admin
     */
    public static boolean canManageUsers(User user) {
        return hasPermission(user, User.Role.ADMIN);
    }

    /**
     * Returns whether the user can modify application statuses (MO role required).
     *
     * @param user the user to check
     * @return {@code true} if the user is an active MO
     */
    public static boolean canModifyApplicationStatus(User user) {
        return hasPermission(user, User.Role.MO);
    }

    /**
     * Returns whether the user can post a new TA position (MO role required).
     *
     * @param user the user to check
     * @return {@code true} if the user is an active MO
     */
    public static boolean canPostPosition(User user) {
        return hasPermission(user, User.Role.MO);
    }

    /**
     * Returns whether the user can view audit logs (ADMIN role required).
     *
     * @param user the user to check
     * @return {@code true} if the user is an active admin
     */
    public static boolean canViewAuditLogs(User user) {
        return hasPermission(user, User.Role.ADMIN);
    }
}