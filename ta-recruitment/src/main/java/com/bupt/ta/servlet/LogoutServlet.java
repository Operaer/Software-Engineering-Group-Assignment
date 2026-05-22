package com.bupt.ta.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles user logout by invalidating the current HTTP session.
 *
 * <p>Mapped to {@code /logout}. Processes GET requests to terminate the session
 * and redirect the user to the home page.</p>
 */
@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends BaseServlet {

    /**
     * Invalidates the current session, if one exists, and redirects to the home page.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if an unexpected servlet error occurs
     * @throws IOException      if the redirect fails
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession(false) != null) {
            req.getSession(false).invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/");
    }
}
