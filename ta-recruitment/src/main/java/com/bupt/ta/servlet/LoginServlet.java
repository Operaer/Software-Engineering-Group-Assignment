package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.storage.UserStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        forwardTo(req, resp, "/index.jsp");
    }

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