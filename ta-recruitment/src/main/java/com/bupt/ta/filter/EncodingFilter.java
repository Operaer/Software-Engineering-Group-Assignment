package com.bupt.ta.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Character encoding filter.
 * Intercepts all requests (/*) and uniformly sets UTF-8 character encoding for each request and response,
 * preventing Chinese character encoding issues and ensuring proper character display within the system.
 */
@WebFilter(filterName = "EncodingFilter", urlPatterns = "/*")
public class EncodingFilter implements Filter {
    /**
     * Filter initialization method.
     * No initialization operations are needed in the current implementation.
     *
     * @param filterConfig the filter configuration object containing initialization parameters
     */
    @Override
    public void init(FilterConfig filterConfig) {
        // no init needed
    }

    /**
     * Performs the filtering logic.
     * Sets UTF-8 character encoding for both the request and response, sets the response content type
     * to text/html; charset=UTF-8, then passes the request to the next component in the filter chain.
     *
     * @param request  the Servlet request object, to be set with UTF-8 encoding
     * @param response the Servlet response object, to be set with UTF-8 encoding and content type
     * @param chain    the filter chain for passing the request to the next filter or target servlet
     * @throws IOException      if an I/O exception occurs during filtering
     * @throws ServletException if a servlet exception occurs during filtering
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        chain.doFilter(request, response);
    }

    /**
     * Filter destruction method.
     * No cleanup operations are needed in the current implementation.
     */
    @Override
    public void destroy() {
        // no cleanup needed
    }
}
