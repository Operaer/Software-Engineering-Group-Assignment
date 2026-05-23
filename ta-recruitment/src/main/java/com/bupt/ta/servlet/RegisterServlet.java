package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.storage.UserStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Servlet handling TA user registration requests.
 * <p>
 * Mapped URL: /register<br>
 * GET request: Displays the registration page; redirects to dashboard if user is already logged in.<br>
 * POST request: Validates registration information (email format, username format and uniqueness,
 * password strength, etc.), creates a new TA account, and redirects to the login page.
 * </p>
 */
@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends BaseServlet {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    /**
     * Handles GET requests to display the registration page.
     * <p>
     * If the user is already logged in, redirects directly to the dashboard;
     * otherwise forwards to the registration page.
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
        forwardTo(req, resp, "/register.jsp");
    }

    /**
     * Handles POST requests to perform user registration.
     * <p>
     * Receives username, email, password, and confirmPassword parameters. Sequentially validates:
     * required fields are not empty, username format (3-20 characters, letters/digits/underscores),
     * email format, password length (at least 6 characters), password confirmation match,
     * username uniqueness, and email uniqueness. Upon successful validation, creates the user and
     * redirects to the login page.
     * </p>
     *
     * @param req  HTTP request containing username, email, password, confirmPassword parameters
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during forwarding or redirect
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        String username = safeTrim(req.getParameter("username"));
        String email = safeTrim(req.getParameter("email")).toLowerCase();
        String password = safeTrim(req.getParameter("password"));
        String confirmPassword = safeTrim(req.getParameter("confirmPassword"));

        req.setAttribute("username", username);
        req.setAttribute("email", email);

        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            req.setAttribute("error", "All fields are required.");
            forwardTo(req, resp, "/register.jsp");
            return;
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            req.setAttribute("error", "Username must be 3 to 20 characters and contain only letters, numbers, or underscores.");
            forwardTo(req, resp, "/register.jsp");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            req.setAttribute("error", "Please enter a valid email address.");
            forwardTo(req, resp, "/register.jsp");
            return;
        }

        if (password.length() < 6) {
            req.setAttribute("error", "Password must be at least 6 characters long.");
            forwardTo(req, resp, "/register.jsp");
            return;
        }

        if (!password.equals(confirmPassword)) {
            req.setAttribute("error", "The two password entries do not match.");
            forwardTo(req, resp, "/register.jsp");
            return;
        }

        UserStorage userStorage = new UserStorage(getServletContext());

        if (userStorage.usernameExists(username)) {
            req.setAttribute("error", "This username is already taken.");
            forwardTo(req, resp, "/register.jsp");
            return;
        }

        if (userStorage.emailExists(email)) {
            req.setAttribute("error", "An account with this email already exists.");
            forwardTo(req, resp, "/register.jsp");
            return;
        }

        try {
            User newUser = new User(username, email, password, User.Role.TA, true);
            userStorage.createUser(newUser);

            req.getSession().setAttribute("registerSuccess",
                    "Your TA account has been created successfully. Please sign in.");
            resp.sendRedirect(req.getContextPath() + "/");
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            forwardTo(req, resp, "/register.jsp");
        }
    }

    /**
     * Safely trims whitespace from both ends of a string.
     * <p>
     * Returns an empty string instead of throwing an exception if the input value is null.
     * </p>
     *
     * @param value The original string to process
     * @return The trimmed string, or an empty string if null
     */
    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
