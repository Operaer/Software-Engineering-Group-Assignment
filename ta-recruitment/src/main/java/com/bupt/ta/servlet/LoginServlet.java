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
 * Servlet handling user login requests.
 * <p>
 * Mapped URL: /login<br>
 * GET request: Displays the login page; redirects to dashboard if user is already logged in.<br>
 * POST request: Validates email and password, creates a user session, and redirects to dashboard.
 * </p>
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends BaseServlet {

    /**
     * Handles GET requests to display the login page.
     * <p>
     * If the user is already logged in, redirects directly to the dashboard page;
     * otherwise forwards to the login page.
     * </p>
     *
     * @param req  HTTP request
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during forwarding or redirect
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
     * Handles POST requests to perform user login verification.
     * <p>
     * Receives email and password parameters, sequentially verifying whether the account exists,
     * whether the account is active, and whether the password is correct. Upon successful verification,
     * invalidates the old session, creates a new session, stores user information in the session,
     * and finally redirects to the dashboard.
     * </p>
     *
     * @param req  HTTP request containing email and password parameters
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during forwarding or redirect
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
