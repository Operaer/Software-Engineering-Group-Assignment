package com.bupt.ta.servlet;

import com.bupt.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet handling dashboard page requests.
 * <p>
 * Mapped URL: /dashboard<br>
 * After verifying the user's login status, stores the current user information
 * as a request attribute and forwards to the dashboard JSP page.
 * </p>
 */
@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends BaseServlet {

    /**
     * Handles GET requests to display the dashboard page.
     * <p>
     * First checks whether the user is logged in; if not, redirects to the home page;
     * if logged in, stores the current user object as a request attribute and forwards
     * to the dashboard JSP.
     * </p>
     *
     * @param req  HTTP request
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during forwarding or redirect
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
