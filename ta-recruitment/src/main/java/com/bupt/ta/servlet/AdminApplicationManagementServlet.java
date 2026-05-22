package com.bupt.ta.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.ApplicationStorage;

@WebServlet(name = "AdminApplicationManagementServlet", urlPatterns = "/secure/admin/application-management")
public class AdminApplicationManagementServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requirePermission(req, resp, User.Role.ADMIN);

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        List<Application> applications = storage.findAll();

        req.setAttribute("applications", applications);
        forwardTo(req, resp, "/secure/admin/application_list.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requirePermission(req, resp, User.Role.ADMIN);

        /**
         * Handles single application status updates from the admin dashboard.
         * Admin users can change the status of any application. The current admin user's
         * email address is recorded as the operator in the audit log for traceability.
         *
         * Request parameters:
         *   - applicationId: the ID of the application to update
         *   - status: target Application.Status value (e.g., "Pending", "Shortlisted", "Accepted", "Rejected")
         *
         * On success, updates the application and displays a success message.
         * On error, displays an error message and reloads the application list.
         */

        String applicationId = req.getParameter("applicationId");
        String status = req.getParameter("status");

        if (applicationId == null || status == null) {
            resp.sendRedirect(req.getContextPath() + "/secure/admin/application-management");
            return;
        }

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        User currentUser = getCurrentUser(req);
        String operator = currentUser != null ? currentUser.getEmail() : "admin";
        try {
            Application.Status newStatus = Application.Status.valueOf(status);
            storage.updateStatus(applicationId, newStatus, operator);
            req.setAttribute("success", "Application status updated");
        } catch (Exception e) {
            req.setAttribute("error", "Error updating status: " + e.getMessage());
        }

        // Reload applications and forward back
        List<Application> applications = storage.findAll();
        req.setAttribute("applications", applications);
        forwardTo(req, resp, "/secure/admin/application_list.jsp");
    }
}