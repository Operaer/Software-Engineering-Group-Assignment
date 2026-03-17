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

        // 这里的登录逻辑仅用于原型演示：任何邮箱/密码均可登录。
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "请输入有效的邮箱和密码。");
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
