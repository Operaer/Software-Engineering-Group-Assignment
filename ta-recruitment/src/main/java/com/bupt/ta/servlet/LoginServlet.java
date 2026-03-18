package com.bupt.ta.servlet;

import com.bupt.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String role = req.getParameter("role");

        // This login logic is only for prototype/demo purposes: any email/password is accepted.
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "Please enter a valid email and password.");
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
            return;
        }

        User user;
        try {
            User.Role userRole = User.Role.valueOf(role);
            user = new User(email, userRole);
        } catch (Exception e) {
            user = new User(email, User.Role.TA);
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", user);

        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/");
    }
}
