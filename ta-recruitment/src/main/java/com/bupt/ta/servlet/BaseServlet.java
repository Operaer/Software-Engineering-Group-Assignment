package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.security.PermissionChecker;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    protected User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        Object value = session != null ? session.getAttribute("currentUser") : null;
        return value instanceof User ? (User) value : null;
    }

    protected boolean isLoggedIn(HttpServletRequest req) {
        User user = getCurrentUser(req);
        return user != null && user.isActive();
    }

    protected boolean requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/");
            return false;
        }
        return true;
    }

    protected boolean requirePermission(HttpServletRequest req, HttpServletResponse resp, User.Role requiredRole) throws IOException {
        User user = getCurrentUser(req);
        if (!PermissionChecker.hasPermission(user, requiredRole)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
            return false;
        }
        return true;
    }

    protected void forwardTo(HttpServletRequest req, HttpServletResponse resp, String path)
            throws IOException, ServletException {
        req.getRequestDispatcher(path).forward(req, resp);
    }
}