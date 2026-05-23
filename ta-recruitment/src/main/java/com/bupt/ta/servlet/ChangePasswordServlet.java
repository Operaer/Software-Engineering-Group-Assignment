package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.storage.UserStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet handling user password change requests.
 * <p>
 * Mapped URL: /secure/account/change-password<br>
 * GET request: Displays the password change page (requires login).<br>
 * POST request: Validates the current password correctness, new password length and confirmation
 * match, updates the password, and refreshes the user information in the session.
 * </p>
 */
@WebServlet(name = "ChangePasswordServlet", urlPatterns = "/secure/account/change-password")
public class ChangePasswordServlet extends BaseServlet {

    /**
     * Handles GET requests to display the password change page.
     * <p>
     * Requires the user to be logged in, otherwise redirects to the home page.
     * </p>
     *
     * @param req  HTTP request
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during forwarding or redirect
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        forwardTo(req, resp, "/secure/account/change_password.jsp");
    }

    /**
     * Handles POST requests to perform the password change operation.
     * <p>
     * Receives currentPassword, newPassword, and confirmPassword parameters. Sequentially validates:
     * required fields are not empty, current password is correct, new password is at least 6
     * characters long, new password differs from the current password, and both new password entries
     * match. Upon successful validation, updates the user password and refreshes the user information
     * in the session.
     * </p>
     *
     * @param req  HTTP request containing currentPassword, newPassword, confirmPassword parameters
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during forwarding or redirect
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
