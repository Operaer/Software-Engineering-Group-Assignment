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
 * Servlet filter that enforces authentication and authorization for
 * all requests under the {@code /secure/} URL pattern. Unauthenticated
 * users are redirected to the home page; authenticated users are further
 * checked against resource-level permissions via {@link PermissionChecker}.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = "/secure/*")
public class AuthFilter implements Filter {

    /**
     * Initializes the filter. No-op in the current prototype; may be used
     * to read init parameters from {@code web.xml} or annotation configuration.
     *
     * @param filterConfig the filter configuration object
     */
    @Override
    public void init(FilterConfig filterConfig) {
        // Can be used to read filter init parameters; not needed for this prototype.
    }

    /**
     * Filters incoming HTTP requests. Checks for an active authenticated user
     * in the session and verifies the user has permission to access the
     * requested resource. Unauthenticated requests are redirected to the
     * home page; unauthorized requests receive an HTTP 403 Forbidden error.
     *
     * @param request  the incoming servlet request
     * @param response the outgoing servlet response
     * @param chain    the filter chain for passing the request further
     * @throws IOException      if an I/O error occurs during filtering
     * @throws ServletException if the request processing fails
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

        // 检查用户是否有权限访问当前资源
        String requestURI = req.getRequestURI().substring(req.getContextPath().length());
        if (!PermissionChecker.canAccessResource(user, requestURI)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions to access this resource");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Cleans up resources held by the filter. No-op in the current prototype
     * as no external resources are allocated during initialization.
     */
    @Override
    public void destroy() {
        // Release resources if needed. No special cleanup required for this prototype.
    }
}
