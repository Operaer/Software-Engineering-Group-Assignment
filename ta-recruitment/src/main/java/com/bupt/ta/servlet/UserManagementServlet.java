package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.storage.UserStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet(name = "UserManagementServlet", urlPatterns = "/secure/admin/user-management")
public class UserManagementServlet extends BaseServlet {

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePermission(req, resp, User.Role.ADMIN)) {
            return;
        }

        String action = req.getParameter("action");
        String email = req.getParameter("email");
        UserStorage userStorage = new UserStorage(getServletContext());

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
                        user.setActive(!user.isActive());
                        userStorage.updateUser(user);
                        req.setAttribute("success", "User status updated: " + email);
                    }
                }

            } else if ("resetPassword".equals(action)) {
                email = email == null ? "" : email.trim().toLowerCase();
                User user = userStorage.findByEmail(email);

                if (user != null) {
                    user.setPassword("default123");
                    userStorage.updateUser(user);
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