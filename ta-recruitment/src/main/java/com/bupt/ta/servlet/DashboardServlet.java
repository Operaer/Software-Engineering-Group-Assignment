package com.bupt.ta.servlet;

import com.bupt.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Serves the role-based dashboard page after login.
 *
 * <p>Mapped to {@code /dashboard}. Requires authentication. Forwards to
 * a unified dashboard JSP which renders role-specific content based on the
 * current user's role (TA or MO).</p>
 */
@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends BaseServlet {

    /**
     * Forwards the authenticated user to the dashboard view after logging in.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }

        User user = getCurrentUser(req);
        req.setAttribute("currentUser", user);
        forwardTo(req, resp, "/WEB-INF/includes/base_dashboard.jsp");
    }
}