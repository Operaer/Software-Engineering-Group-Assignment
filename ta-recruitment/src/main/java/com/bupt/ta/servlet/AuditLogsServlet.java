package com.bupt.ta.servlet;

import com.bupt.ta.model.OperationLogEntry;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.OperationLogStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Displays administrative operation logs and applies optional filtering by
 * operator, action type, and date range.
 */
@WebServlet(name = "AuditLogsServlet", urlPatterns = "/secure/admin/audit-logs")
public class AuditLogsServlet extends BaseServlet {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private static final DateTimeFormatter LEGACY_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private Instant parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value, DISPLAY_FORMATTER).atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(value, LEGACY_INPUT_FORMATTER).atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception ignored) {
        }

        return null;
    }

    private String formatValue(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        try {
            LocalDateTime dateTime = LocalDateTime.parse(value, DISPLAY_FORMATTER);
            return dateTime.format(DISPLAY_FORMATTER);
        } catch (Exception ignored) {
        }

        try {
            LocalDateTime dateTime = LocalDateTime.parse(value, LEGACY_INPUT_FORMATTER);
            return dateTime.format(DISPLAY_FORMATTER);
        } catch (Exception ignored) {
        }

        return value;
    }

    /**
     * Retrieves stored operation logs and applies optional filters for
     * operator, action type, and date range.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePermission(req, resp, User.Role.ADMIN)) {
            return;
        }

        OperationLogStorage storage = new OperationLogStorage(getServletContext());
        List<OperationLogEntry> entries = storage.findAll();

        String operatorFilter = req.getParameter("operator");
        String actionTypeFilter = req.getParameter("actionType");
        String fromParam = req.getParameter("from");
        String toParam = req.getParameter("to");

        Instant fromInstant = null;
        Instant toInstant = null;
        if (fromParam != null && !fromParam.isBlank()) {
            fromInstant = parseDateTime(fromParam);
            if (fromInstant == null) {
                req.setAttribute("filterError", "Invalid start date/time format.");
            }
        }
        if (toParam != null && !toParam.isBlank()) {
            toInstant = parseDateTime(toParam);
            if (toInstant == null) {
                req.setAttribute("filterError", "Invalid end date/time format.");
            }
        }

        fromParam = formatValue(fromParam);
        toParam = formatValue(toParam);

        List<OperationLogEntry> filtered = new ArrayList<>();
        for (OperationLogEntry entry : entries) {
            if (operatorFilter != null && !operatorFilter.isBlank()
                    && !entry.getOperator().toLowerCase().contains(operatorFilter.trim().toLowerCase())) {
                continue;
            }
            if (actionTypeFilter != null && !actionTypeFilter.isBlank()
                    && !entry.getActionType().toLowerCase().contains(actionTypeFilter.trim().toLowerCase())) {
                continue;
            }
            if (fromInstant != null && entry.getTimestamp().isBefore(fromInstant)) {
                continue;
            }
            if (toInstant != null && entry.getTimestamp().isAfter(toInstant)) {
                continue;
            }
            filtered.add(entry);
        }

        filtered.sort(Comparator.comparing(OperationLogEntry::getTimestamp).reversed());

        req.setAttribute("logs", filtered);
        req.setAttribute("operator", operatorFilter);
        req.setAttribute("actionType", actionTypeFilter);
        req.setAttribute("from", fromParam);
        req.setAttribute("to", toParam);
        forwardTo(req, resp, "/secure/admin/audit_logs.jsp");
    }
}
