package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.security.PermissionChecker;
import com.bupt.ta.storage.UserStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet(name = "UserManagementServlet", urlPatterns = "/secure/admin/user-management")
public class UserManagementServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requirePermission(req, resp, User.Role.ADMIN);

        UserStorage userStorage = new UserStorage(getServletContext());
        Map<String, User> users = userStorage.getAllUsers();

        req.setAttribute("users", users);
        forwardTo(req, resp, "/secure/admin/user_management.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requirePermission(req, resp, User.Role.ADMIN);

        String action = req.getParameter("action");
        String email = req.getParameter("email");

        if (email == null || email.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/secure/admin/user-management");
            return;
        }

        UserStorage userStorage = new UserStorage(getServletContext());

        try {
            if ("resetPassword".equals(action)) {
                User user = userStorage.findByEmail(email);
                if (user != null) {
                    user.setPassword("default123");
                    userStorage.updateUser(user);
                    req.setAttribute("success", "Password reset for user: " + email);
                }
            } else if ("toggleStatus".equals(action)) {
                User user = userStorage.findByEmail(email);
                if (user != null) {
                    user.setActive(!user.isActive());
                    userStorage.updateUser(user);
                    req.setAttribute("success", "Status toggled for user: " + email);
                }
            } else if ("delete".equals(action)) {
                userStorage.deleteUser(email);
                req.setAttribute("success", "User deleted: " + email);
            } else if ("create".equals(action)) {
                String password = req.getParameter("password");
                String roleStr = req.getParameter("role");
                if (password != null && !password.isBlank() && roleStr != null) {
                    User.Role role = User.Role.valueOf(roleStr);
                    User newUser = new User(email, password, role);
                    userStorage.createUser(newUser);
                    req.setAttribute("success", "User created: " + email);
                }
            }
        } catch (Exception e) {
            req.setAttribute("error", "Error performing action: " + e.getMessage());
        }

        doGet(req, resp);
    }
}