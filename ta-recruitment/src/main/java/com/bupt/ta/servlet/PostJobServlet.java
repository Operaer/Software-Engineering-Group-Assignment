package com.bupt.ta.servlet;

import com.bupt.ta.model.Job;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.JobStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Servlet for creating new TA position listings by Module Organizers (MO).
 *
 * <p>Mapped to {@code /post-job}. Handles GET requests to display the
 * position creation form and POST requests to validate and persist
 * new job positions with title, module code, workload, requirements,
 * and deadline fields.</p>
 */
@WebServlet("/post-job")
public class PostJobServlet extends BaseServlet {

    /**
     * Displays the new position creation form.
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @throws ServletException if forwarding to the JSP fails
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        if (!requirePermission(req, resp, User.Role.MO)) return;

        forwardTo(req, resp, "/secure/mo/post_position.jsp");
    }

    /**
     * Processes the position creation form submission.
     *
     * <p>Validates all required fields (title, moduleCode, workload,
     * requirements, deadline), checks that the deadline is a future date,
     * standardizes field formats, and persists the new job position.
     * Returns the creation form with an error message if validation fails.</p>
     *
     * @param req the HTTP request containing position data parameters
     * @param resp the HTTP response
     * @throws ServletException if forwarding to the JSP fails
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        if (!requirePermission(req, resp, User.Role.MO)) return;

        String title = req.getParameter("title");
        String moduleCode = req.getParameter("moduleCode");
        String workload = req.getParameter("workload");
        String requirements = req.getParameter("requirements");
        String deadlineStr = req.getParameter("deadline");

        // Validate required fields
        if (title == null || title.trim().isEmpty() ||
            moduleCode == null || moduleCode.trim().isEmpty() ||
            workload == null || workload.trim().isEmpty() ||
            requirements == null || requirements.trim().isEmpty() ||
            deadlineStr == null || deadlineStr.trim().isEmpty()) {
            req.setAttribute("error", "All fields are required.");
            forwardTo(req, resp, "/secure/mo/post_position.jsp");
            return;
        }

        // Parse and validate deadline
        LocalDate deadline;
        try {
            deadline = LocalDate.parse(deadlineStr);
            if (deadline.isBefore(LocalDate.now())) {
                req.setAttribute("error", "Deadline must be a future date.");
                forwardTo(req, resp, "/secure/mo/post_position.jsp");
                return;
            }
        } catch (DateTimeParseException e) {
            req.setAttribute("error", "Invalid deadline format.");
            forwardTo(req, resp, "/secure/mo/post_position.jsp");
            return;
        }

        // Standardize fields (trim and capitalize)
        title = title.trim();
        moduleCode = moduleCode.trim().toUpperCase();
        workload = workload.trim();
        requirements = requirements.trim();

        // Save job
        JobStorage jobStorage = new JobStorage(getServletContext());
        User currentUser = getCurrentUser(req);
        jobStorage.createNew(title, moduleCode, workload, requirements, deadline, currentUser.getEmail());

        req.setAttribute("success", "Position posted successfully.");
        forwardTo(req, resp, "/secure/mo/post_position.jsp");
    }
}