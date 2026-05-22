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
 * Servlet for administrators to manage TA applications.
 *
 * <p>Mapped to {@code /secure/admin/application-management}. Handles GET requests
 * to list all applications and POST requests to update individual application
 * statuses (e.g., Shortlisted, Accepted, Rejected). All status changes are
 * recorded with the current admin's email for audit traceability.</p>
 */
@WebServlet(name = "AdminApplicationManagementServlet", urlPatterns = "/secure/admin/application-management")
public class AdminApplicationManagementServlet extends BaseServlet {

    /**
     * Retrieves and displays all TA applications for admin review.
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if forwarding to the JSP fails
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requirePermission(req, resp, User.Role.ADMIN);

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        List<Application> applications = storage.findAll();

        req.setAttribute("applications", applications);
        forwardTo(req, resp, "/secure/admin/application_list.jsp");
    }

    /**
     * Handles single application status updates submitted by an administrator.
     *
     * <p>Accepts {@code applicationId} and {@code status} parameters. The current
     * admin user's email is recorded as the operator for audit traceability.
     * On success, a confirmation message is displayed. On error, an error
     * message is shown and the application list is reloaded.</p>
     *
     * @param req the HTTP request containing applicationId and status parameters
     * @param resp the HTTP response
     * @throws ServletException if forwarding to the JSP fails
     * @throws IOException if an I/O error occurs
     */
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