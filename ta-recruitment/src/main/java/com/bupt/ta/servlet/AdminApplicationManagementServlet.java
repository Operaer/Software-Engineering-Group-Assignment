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

/**
 * Admin Application Management Servlet.
 *
 * <p>Provides administrators with the ability to view all job applications and update
 * individual application statuses. Administrators can change the application status to
 * Pending, Shortlisted, Accepted, or Rejected. The operator's email is recorded in the
 * audit log.</p>
 */
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
