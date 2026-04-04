package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.security.PermissionChecker;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    protected User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null ? (User) session.getAttribute("currentUser") : null;
    }

    protected boolean isLoggedIn(HttpServletRequest req) {
        User user = getCurrentUser(req);
        return user != null && user.isActive();
    }

    protected void requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    protected void requirePermission(HttpServletRequest req, HttpServletResponse resp, User.Role requiredRole) throws IOException {
        User user = getCurrentUser(req);
        if (!PermissionChecker.hasPermission(user, requiredRole)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
            return;
        }
    }

    protected void forwardTo(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException, javax.servlet.ServletException {
        req.getRequestDispatcher(path).forward(req, resp);
    }
}