package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.security.PermissionChecker;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Abstract base servlet providing common helper methods for all servlets in the application.
 *
 * <p>Convenience methods include session-based user retrieval, login and permission
 * verification, and request forwarding. Subclasses inherit these methods to avoid
 * duplicating common Servlet API logic.</p>
 */
public abstract class BaseServlet extends HttpServlet {

    /**
     * Retrieves the currently authenticated user from the HTTP session, if any.
     *
     * @param req the HTTP request containing the session
     * @return the logged-in {@link User}, or {@code null} if no session exists or no user is logged in
     */
    protected User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        Object value = session != null ? session.getAttribute("currentUser") : null;
        return value instanceof User ? (User) value : null;
    }

    /**
     * Checks whether a user is currently authenticated and active in the session.
     *
     * @param req the HTTP request
     * @return {@code true} if a non-null, active user is present in the session
     */
    protected boolean isLoggedIn(HttpServletRequest req) {
        User user = getCurrentUser(req);
        return user != null && user.isActive();
    }

    /**
     * Ensures that the requester is logged in; redirects to the home page otherwise.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @return {@code true} if the user is logged in, {@code false} after redirecting
     * @throws IOException if an I/O error occurs during the redirect
     */
    protected boolean requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/");
            return false;
        }
        return true;
    }

    /**
     * Ensures the authenticated user has the required role; sends a 403 Forbidden error otherwise.
     *
     * @param req          the HTTP request
     * @param resp         the HTTP response
     * @param requiredRole the minimum role required to access the resource
     * @return {@code true} if the user has the required permission, {@code false} after sending the error
     * @throws IOException if an I/O error occurs while sending the error
     */
    protected boolean requirePermission(HttpServletRequest req, HttpServletResponse resp, User.Role requiredRole) throws IOException {
        User user = getCurrentUser(req);
        if (!PermissionChecker.hasPermission(user, requiredRole)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
            return false;
        }
        return true;
    }

    /**
     * Forwards the current request and response to the specified path via the servlet container.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @param path the target path for the forward (typically a JSP)
     * @throws IOException      if an I/O error occurs during forwarding
     * @throws ServletException if the target resource cannot be forwarded to
     */
    protected void forwardTo(HttpServletRequest req, HttpServletResponse resp, String path)
            throws IOException, ServletException {
        req.getRequestDispatcher(path).forward(req, resp);
    }
}