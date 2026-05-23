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
 * Abstract base Servlet providing common methods for all Servlets in the TA recruitment system.
 * <p>
 * Encapsulates common functionality such as retrieving the currently logged-in user,
 * checking login status, permission verification, and request forwarding.
 * All concrete business Servlets should extend this class.
 * </p>
 */
public abstract class BaseServlet extends HttpServlet {

    /**
     * Retrieves the logged-in user object from the current session.
     *
     * @param req HTTP request used to obtain session information
     * @return The currently logged-in User object, or null if not logged in or session does not exist
     */
    protected User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        Object value = session != null ? session.getAttribute("currentUser") : null;
        return value instanceof User ? (User) value : null;
    }

    /**
     * Checks whether the current user is logged in and the account is active.
     *
     * @param req HTTP request used to obtain the current user
     * @return true if the user is logged in and the account is active, false otherwise
     */
    protected boolean isLoggedIn(HttpServletRequest req) {
        User user = getCurrentUser(req);
        return user != null && user.isActive();
    }

    /**
     * Requires the user to be logged in, otherwise redirects to the home page.
     *
     * @param req  HTTP request
     * @param resp HTTP response
     * @return true if logged in, false otherwise
     * @throws IOException if an IO error occurs during redirect
     */
    protected boolean requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/");
            return false;
        }
        return true;
    }

    /**
     * Requires the current user to have the specified role permission, otherwise returns a 403 error.
     *
     * @param req          HTTP request
     * @param resp         HTTP response
     * @param requiredRole The required role permission
     * @return true if the user has the required permission, false otherwise
     * @throws IOException if an IO error occurs while sending the error response
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
     * Forwards the request to the specified path.
     *
     * @param req  HTTP request
     * @param resp HTTP response
     * @param path The target forward path
     * @throws IOException      if an IO error occurs during forwarding
     * @throws ServletException if a Servlet exception occurs during forwarding
     */
    protected void forwardTo(HttpServletRequest req, HttpServletResponse resp, String path)
            throws IOException, ServletException {
        req.getRequestDispatcher(path).forward(req, resp);
    }
}
