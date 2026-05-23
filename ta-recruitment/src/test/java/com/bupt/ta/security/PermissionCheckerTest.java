package com.bupt.ta.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.bupt.ta.model.User;

/**
 * Unit tests for the {@link PermissionChecker} utility class.
 */
class PermissionCheckerTest {

    /**
     * Verifies that users with ADMIN role can access all secure resources.
     */
    @Test
    void adminCanAccessAllSecureResources() {
        User admin = new User("admin", "admin@example.com", "pwd", User.Role.ADMIN, true);

        assertTrue(PermissionChecker.canAccessResource(admin, "/secure/admin/dashboard"));
        assertTrue(PermissionChecker.canAccessResource(admin, "/secure/mo/manage_positions"));
        assertTrue(PermissionChecker.canAccessResource(admin, "/secure/ta/profile"));
        assertTrue(PermissionChecker.canAccessResource(admin, "/dashboard"));
    }

    /**
     * Verifies that users with MO role can only access MO and TA resources.
     */
    @Test
    void moCanAccessMoAndTaResourcesOnly() {
        User mo = new User("mo", "mo@example.com", "pwd", User.Role.MO, true);

        assertFalse(PermissionChecker.canAccessResource(mo, "/secure/admin/dashboard"));
        assertTrue(PermissionChecker.canAccessResource(mo, "/secure/mo/manage_positions"));
        assertTrue(PermissionChecker.canAccessResource(mo, "/secure/ta/profile"));
        assertTrue(PermissionChecker.canAccessResource(mo, "/dashboard"));
    }

    /**
     * Verifies that users with TA role can only access TA-level resources.
     */
    @Test
    void taCanAccessTaResourcesOnly() {
        User ta = new User("ta", "ta@example.com", "pwd", User.Role.TA, true);

        assertFalse(PermissionChecker.canAccessResource(ta, "/secure/admin/dashboard"));
        assertFalse(PermissionChecker.canAccessResource(ta, "/secure/mo/manage_positions"));
        assertTrue(PermissionChecker.canAccessResource(ta, "/secure/ta/profile"));
        assertTrue(PermissionChecker.canAccessResource(ta, "/dashboard"));
    }

    /**
     * Tests that utility permission methods respect the role hierarchy.
     */
    @Test
    void utilityMethodsRespectRoleHierarchy() {
        User admin = new User("admin", "admin@example.com", "pwd", User.Role.ADMIN, true);
        User mo = new User("mo", "mo@example.com", "pwd", User.Role.MO, true);
        User ta = new User("ta", "ta@example.com", "pwd", User.Role.TA, true);

        assertTrue(PermissionChecker.canManageUsers(admin));
        assertFalse(PermissionChecker.canManageUsers(mo));

        assertTrue(PermissionChecker.canModifyApplicationStatus(mo));
        assertFalse(PermissionChecker.canModifyApplicationStatus(ta));

        assertTrue(PermissionChecker.canViewAuditLogs(admin));
        assertFalse(PermissionChecker.canViewAuditLogs(mo));
    }
}
