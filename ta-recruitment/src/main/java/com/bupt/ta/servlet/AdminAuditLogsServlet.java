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
 * Admin Audit Log Viewing Servlet.
 *
 * <p>Provides administrators with the ability to view system operation audit logs. It
 * supports filtering by operator, action type, and time range (start and end time)
 * to help track critical operations in the system.</p>
 */
@WebServlet(name = "AdminAuditLogsServlet", urlPatterns = "/secure/admin/audit-logs")
public class AdminAuditLogsServlet extends BaseServlet {

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
