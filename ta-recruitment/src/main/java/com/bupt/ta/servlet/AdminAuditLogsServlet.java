package com.bupt.ta.servlet;

import com.bupt.ta.model.AuditLogEntry;
import com.bupt.ta.storage.AuditLogStorage;
import com.bupt.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Servlet for administrators to view and query audit log entries.
 *
 * <p>Mapped to {@code /secure/admin/audit-logs}. Provides filtering
 * by operator, action type, and a time range (from/to). Requires
 * the user to pass the permission check defined in
 * {@code PermissionChecker.canViewAuditLogs()}.</p>
 */
@WebServlet(name = "AdminAuditLogsServlet", urlPatterns = "/secure/admin/audit-logs")
public class AdminAuditLogsServlet extends BaseServlet {

    /**
     * Handles GET requests to display filtered audit log entries.
     *
     * <p>Supports optional filtering by {@code operator}, {@code actionType},
     * and a time range ({@code from}, {@code to}). Time parameters are parsed
     * as ISO-8601 instant strings; invalid values are silently ignored.</p>
     *
     * @param req the HTTP request containing optional filter parameters
     * @param resp the HTTP response
     * @throws ServletException if forwarding to the JSP fails
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        User user = getCurrentUser(req);
        if (!com.bupt.ta.security.PermissionChecker.canViewAuditLogs(user)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
            return;
        }

        String operator = req.getParameter("operator");
        String action = req.getParameter("actionType");
        String fromStr = req.getParameter("from");
        String toStr = req.getParameter("to");

        Instant from = null;
        Instant to = null;
        try {
            if (fromStr != null && !fromStr.isBlank()) from = Instant.parse(fromStr);
        } catch (DateTimeParseException ignored) {}
        try {
            if (toStr != null && !toStr.isBlank()) to = Instant.parse(toStr);
        } catch (DateTimeParseException ignored) {}

        AuditLogStorage storage = new AuditLogStorage(getServletContext());
        List<AuditLogEntry> entries = storage.query(operator, action, from, to);
        req.setAttribute("operatorOptions", storage.findOperators());
        req.setAttribute("actionTypeOptions", storage.findActionTypes());

        req.setAttribute("entries", entries);
        forwardTo(req, resp, "/secure/admin/audit_logs.jsp");
    }
}
