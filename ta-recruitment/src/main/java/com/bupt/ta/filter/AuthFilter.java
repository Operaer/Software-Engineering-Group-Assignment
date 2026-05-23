package com.bupt.ta.filter;

import com.bupt.ta.model.User;
import com.bupt.ta.security.PermissionChecker;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Authentication and authorization filter.
 * Intercepts all requests to /secure/* paths, checks whether the user is logged in and active,
 * and further verifies whether the user has access permission to the requested resource.
 * Requests that fail authentication or lack sufficient permissions will be redirected or return a 403 error.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = "/secure/*")
public class AuthFilter implements Filter {
    /**
     * Filter initialization method.
     * Can be used to read filter initialization parameters; no additional initialization is needed for this prototype.
     *
     * @param filterConfig the filter configuration object containing initialization parameters
     */
    @Override
    public void init(FilterConfig filterConfig) {
        // Can be used to read filter init parameters; not needed for this prototype.
    }

    /**
     * Performs the filtering logic.
     * Retrieves the current user information from the session, verifies the user's login status
     * and account activation state, then uses PermissionChecker to verify whether the user
     * has permission to access the requested resource.
     *
     * @param request  the Servlet request object, cast to HttpServletRequest to obtain the session
     * @param response the Servlet response object, used for redirect or sending error status codes
     * @param chain    the filter chain for passing the request to the next filter or target servlet
     * @throws IOException      if an I/O exception occurs during redirect or error sending
     * @throws ServletException if a servlet exception occurs during filtering
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        if (user == null || !user.isActive()) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        // Check if the user has permission to access the current resource
        String requestURI = req.getRequestURI().substring(req.getContextPath().length());
        if (!PermissionChecker.canAccessResource(user, requestURI)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions to access this resource");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Filter destruction method.
     * Used to release resources held by the filter; no special cleanup is needed for this prototype.
     */
    @Override
    public void destroy() {
        // Release resources if needed. No special cleanup required for this prototype.
    }
}
