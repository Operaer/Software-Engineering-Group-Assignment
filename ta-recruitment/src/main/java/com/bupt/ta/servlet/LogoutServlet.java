package com.bupt.ta.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet handling user logout requests.
 * <p>
 * Mapped URL: /logout<br>
 * Invalidates the current session, clears the login state, and redirects to the home page.
 * </p>
 */
@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends BaseServlet {

    /**
     * Handles GET requests to perform the logout operation.
     * <p>
     * If a valid session exists, invalidates it; finally redirects to the application home page.
     * </p>
     *
     * @param req  HTTP request
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during redirect
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession(false) != null) {
            req.getSession(false).invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/");
    }
}
