package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.storage.OperationLogStorage;
import com.bupt.ta.storage.UserStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Servlet for admin user management operations.
 * <p>
 * Provides a secured Admin interface for listing users and handling
 * user lifecycle actions such as creation, status toggle, password reset,
 * and deletion.
 * </p>
 */
@WebServlet(name = "UserManagementServlet", urlPatterns = "/secure/admin/user-management")
public class UserManagementServlet extends BaseServlet {

    /**
     * Displays the user management page for administrators.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePermission(req, resp, User.Role.ADMIN)) {
            return;
        }

        UserStorage userStorage = new UserStorage(getServletContext());
        Map<String, User> users = userStorage.getAllUsers();

        req.setAttribute("users", users);
        forwardTo(req, resp, "/secure/admin/user_management.jsp");
    }

    /**
     * Handles admin user lifecycle actions sent from the management page.
     * <p>
     * Supported actions include creating users, toggling account status,
     * resetting passwords, and deleting users. Only ADMIN users may perform
     * these operations.
     * </p>
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePermission(req, resp, User.Role.ADMIN)) {
            return;
        }

        String action = req.getParameter("action");
        String email = req.getParameter("email");
        UserStorage userStorage = new UserStorage(getServletContext());
        OperationLogStorage logStorage = new OperationLogStorage(getServletContext());

        try {
            if ("create".equals(action)) {
                String password = req.getParameter("password");
                String roleStr = req.getParameter("role");
                String username = req.getParameter("username");

                email = email == null ? "" : email.trim().toLowerCase();
                password = password == null ? "" : password.trim();
                username = username == null ? "" : username.trim();

                if (email.isBlank() || password.isBlank() || roleStr == null || roleStr.isBlank()) {
                    req.setAttribute("error", "Email, password and role are required.");
                } else {
                    User.Role role = User.Role.valueOf(roleStr);
                    if (username.isBlank()) {
                        int at = email.indexOf('@');
                        username = at > 0 ? email.substring(0, at) : email;
                    }

                    User newUser = new User(username, email, password, role, true);
                    userStorage.createUser(newUser);
                    logStorage.record(getCurrentUser(req).getEmail(), "Create Account", email, "Created user with role " + role + ".");
                    req.setAttribute("success", "User created successfully: " + email);
                }

            } else if ("toggleStatus".equals(action)) {
                email = email == null ? "" : email.trim().toLowerCase();
                User user = userStorage.findByEmail(email);

                if (user == null) {
                    req.setAttribute("error", "User not found.");
                } else {
                    User currentUser = getCurrentUser(req);

                    if (currentUser != null && email.equalsIgnoreCase(currentUser.getEmail()) && user.isActive()) {
                        req.setAttribute("error", "You cannot disable the currently logged-in admin account.");
                    } else if (user.getRole() == User.Role.ADMIN && user.isActive() && userStorage.countActiveAdmins() <= 1) {
                        req.setAttribute("error", "The last active administrator cannot be disabled.");
                    } else {
                        boolean newStatus = !user.isActive();
                        user.setActive(newStatus);
                        userStorage.updateUser(user);
                        logStorage.record(getCurrentUser(req).getEmail(), "Account Status Change", email,
                                "Set account " + email + " to " + (newStatus ? "active" : "disabled") + ".");
                        req.setAttribute("success", "User status updated: " + email);
                    }
                }

            } else if ("resetPassword".equals(action)) {
                email = email == null ? "" : email.trim().toLowerCase();
                User user = userStorage.findByEmail(email);

                if (user != null) {
                    user.setPassword("default123");
                    userStorage.updateUser(user);
                    logStorage.record(getCurrentUser(req).getEmail(), "Password Reset", email,
                            "Reset password for user " + email + ".");
                    req.setAttribute("success", "Password reset successfully for: " + email);
                } else {
                    req.setAttribute("error", "User not found.");
                }

            } else if ("delete".equals(action)) {
                email = email == null ? "" : email.trim().toLowerCase();
                User targetUser = userStorage.findByEmail(email);
                User currentUser = getCurrentUser(req);

                if (targetUser == null) {
                    req.setAttribute("error", "User not found.");
                } else if (currentUser != null && email.equalsIgnoreCase(currentUser.getEmail())) {
                    req.setAttribute("error", "You cannot delete the currently logged-in admin account.");
                } else if (targetUser.getRole() == User.Role.ADMIN && userStorage.countAdmins() <= 1) {
                    req.setAttribute("error", "The last administrator account cannot be deleted.");
                } else {
                    userStorage.deleteUser(email);
                    logStorage.record(getCurrentUser(req).getEmail(), "Delete Account", email,
                            "Deleted user account " + email + ".");
                    req.setAttribute("success", "User deleted successfully: " + email);
                }
            }

        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
        } catch (Exception e) {
            req.setAttribute("error", "Error performing action: " + e.getMessage());
        }

        doGet(req, resp);
    }
}