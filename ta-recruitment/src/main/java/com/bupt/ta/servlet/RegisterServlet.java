package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.storage.UserStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends BaseServlet {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        forwardTo(req, resp, "/register.jsp");
    }

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

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}