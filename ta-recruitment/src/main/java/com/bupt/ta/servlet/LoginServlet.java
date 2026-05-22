package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.storage.UserStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handles user authentication via login form submission.
 *
 * <p>Mapped to {@code /login}. The {@code doGet} method forwards logged-in users
 * to the dashboard and all others to the login page. The {@code doPost} method
 * validates credentials against {@link com.bupt.ta.storage.UserStorage}, enforces
 * account-active checks, and creates an authenticated HTTP session on success.</p>
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends BaseServlet {

    /**
     * Displays the login page or redirects already-authenticated users to the dashboard.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        forwardTo(req, resp, "/index.jsp");
    }

    /**
     * Authenticates a user with email and password credentials.
     *
     * <p>Validates that both fields are non-blank, verifies the account exists and
     * is active, and checks the password. On success, invalidates any existing
     * session and creates a new one with the {@code currentUser} attribute set.
     * On failure, sets a descriptive error message and re-displays the login page.</p>
     *
     * @param req  the HTTP request containing {@code email} and {@code password} parameters
     * @param resp the HTTP response
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        email = email == null ? "" : email.trim().toLowerCase();
        password = password == null ? "" : password.trim();

        if (email.isBlank() || password.isBlank()) {
            req.setAttribute("error", "Please enter both email and password.");
            req.setAttribute("email", email);
            forwardTo(req, resp, "/index.jsp");
            return;
        }

        UserStorage userStorage = new UserStorage(getServletContext());
        User user = userStorage.findByEmail(email);

        if (user == null) {
            req.setAttribute("error", "Account does not exist.");
            req.setAttribute("email", email);
            forwardTo(req, resp, "/index.jsp");
            return;
        }

        if (!user.isActive()) {
            req.setAttribute("error", "This account has been disabled.");
            req.setAttribute("email", email);
            forwardTo(req, resp, "/index.jsp");
            return;
        }

        if (!user.passwordMatches(password)) {
            req.setAttribute("error", "Incorrect password.");
            req.setAttribute("email", email);
            forwardTo(req, resp, "/index.jsp");
            return;
        }

        HttpSession oldSession = req.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", user);

        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}