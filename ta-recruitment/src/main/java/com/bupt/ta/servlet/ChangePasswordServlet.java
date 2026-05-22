package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.storage.UserStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles authenticated password changes for all user roles.
 *
 * <p>Mapped to {@code /secure/account/change-password}. Requires the user to be
 * logged in. The {@code doGet} method displays the change-password form. The
 * {@code doPost} method validates the current password, ensures the new password
 * meets length and uniqueness requirements, and persists the update.</p>
 */
@WebServlet(name = "ChangePasswordServlet", urlPatterns = "/secure/account/change-password")
public class ChangePasswordServlet extends BaseServlet {

    /**
     * Shows the change-password form to an authenticated user.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        forwardTo(req, resp, "/secure/account/change_password.jsp");
    }

    /**
     * Processes a password change request.
     *
     * <p>Validates that the current password is correct, the new password is at
     * least 6 characters and differs from the current one, and the confirmation
     * matches. On success, updates the user record in storage and refreshes the
     * session attribute.</p>
     *
     * @param req  the HTTP request containing {@code currentPassword},
     *             {@code newPassword}, and {@code confirmPassword} parameters
     * @param resp the HTTP response
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }

        User currentUser = getCurrentUser(req);
        String currentPassword = req.getParameter("currentPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        currentPassword = currentPassword == null ? "" : currentPassword.trim();
        newPassword = newPassword == null ? "" : newPassword.trim();
        confirmPassword = confirmPassword == null ? "" : confirmPassword.trim();

        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            req.setAttribute("error", "All password fields are required.");
            forwardTo(req, resp, "/secure/account/change_password.jsp");
            return;
        }

        if (!currentUser.passwordMatches(currentPassword)) {
            req.setAttribute("error", "Current password is incorrect.");
            forwardTo(req, resp, "/secure/account/change_password.jsp");
            return;
        }

        if (newPassword.length() < 6) {
            req.setAttribute("error", "The new password must be at least 6 characters long.");
            forwardTo(req, resp, "/secure/account/change_password.jsp");
            return;
        }

        if (newPassword.equals(currentPassword)) {
            req.setAttribute("error", "The new password must be different from the current password.");
            forwardTo(req, resp, "/secure/account/change_password.jsp");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            req.setAttribute("error", "The two new password entries do not match.");
            forwardTo(req, resp, "/secure/account/change_password.jsp");
            return;
        }

        UserStorage userStorage = new UserStorage(getServletContext());
        User latestUser = userStorage.findByEmail(currentUser.getEmail());

        if (latestUser == null) {
            req.setAttribute("error", "Account not found.");
            forwardTo(req, resp, "/secure/account/change_password.jsp");
            return;
        }

        latestUser.setPassword(newPassword);
        userStorage.updateUser(latestUser);

        req.getSession().setAttribute("currentUser", latestUser);
        req.setAttribute("success", "Password changed successfully.");

        forwardTo(req, resp, "/secure/account/change_password.jsp");
    }
}