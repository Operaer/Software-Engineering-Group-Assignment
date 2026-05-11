package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.User;
import com.bupt.ta.security.PermissionChecker;
import com.bupt.ta.storage.ApplicationStorage;
import com.bupt.ta.storage.OperationLogStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Provides application review and status update capabilities for
 * module organizers, with operation logging for each change.
 */
@WebServlet(name = "ApplicationManagementServlet", urlPatterns = "/secure/mo/application-management")
public class ApplicationManagementServlet extends BaseServlet {

    /**
     * Renders the application management list for MO users.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePermission(req, resp, User.Role.MO)) {
            return;
        }

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        List<Application> applications = storage.findAll();

        req.setAttribute("applications", applications);
        forwardTo(req, resp, "/secure/mo/application_list.jsp");
    }

    /**
     * Updates the selected application status and records the operation.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePermission(req, resp, User.Role.MO)) {
            return;
        }

        String applicationId = req.getParameter("applicationId");
        String status = req.getParameter("status");

        if (applicationId == null || status == null) {
            resp.sendRedirect(req.getContextPath() + "/secure/mo/application-management");
            return;
        }

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        try {
            Application.Status newStatus = Application.Status.valueOf(status);
            storage.updateStatus(applicationId, newStatus);
            User currentUser = getCurrentUser(req);
            new OperationLogStorage(getServletContext())
                    .record(currentUser.getEmail(), "Application Status Change", applicationId,
                            "Updated application status to " + newStatus + ".");
            req.setAttribute("success", "Application status updated");
        } catch (Exception e) {
            req.setAttribute("error", "Error updating status: " + e.getMessage());
        }

        doGet(req, resp);
    }
}