package com.bupt.ta.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link User} model class.
 */
class UserTest {

    /**
     * Verifies the username is derived from the email prefix when a null username is provided.
     */
    @Test
    void buildDefaultUsernameFromEmail() {
        User user = new User(null, "alice@example.com", "password", User.Role.TA, true);
        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
    }

    /**
     * Tests password matching with both correct and incorrect passwords.
     */
    @Test
    void passwordMatchesWorksCorrectly() {
        User user = new User("alice", "alice@example.com", "secret", User.Role.TA, true);
        assertTrue(user.passwordMatches("secret"));
        assertFalse(user.passwordMatches("wrong"));
    }

    /**
     * Verifies the role hierarchy permission ordering is correct.
     */
    @Test
    void rolePermissionOrderingIsCorrect() {
        User ta = new User("ta", "ta@example.com", "pwd", User.Role.TA, true);
        User mo = new User("mo", "mo@example.com", "pwd", User.Role.MO, true);
        User admin = new User("admin", "admin@example.com", "pwd", User.Role.ADMIN, true);

        assertTrue(admin.hasPermission(User.Role.MO));
        assertTrue(admin.hasPermission(User.Role.TA));
        assertTrue(mo.hasPermission(User.Role.TA));
        assertFalse(ta.hasPermission(User.Role.MO));
        assertFalse(ta.hasPermission(User.Role.ADMIN));
    }

    /**
     * Tests that getStatusText returns "Active" or "Disabled" based on the active flag.
     */
    @Test
    void getStatusTextReturnsActiveOrDisabled() {
        User activeUser = new User("active", "active@example.com", "pwd", User.Role.TA, true);
        User disabledUser = new User("disabled", "disabled@example.com", "pwd", User.Role.TA, false);

        assertEquals("Active", activeUser.getStatusText());
        assertEquals("Disabled", disabledUser.getStatusText());
    }
}
