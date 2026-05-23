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
 * Servlet handling MO requests to post new positions.
 * <p>
 * Mapped URL: /post-job<br>
 * GET request: Displays the position posting page (requires MO permission).<br>
 * POST request: Receives position information (title, module code, workload, requirements, deadline),
 * validates required fields and deadline validity, saves the position, and confirms successful posting.
 * </p>
 */
@WebServlet("/post-job")
public class PostJobServlet extends BaseServlet {

    /**
     * Handles GET requests to display the position posting page.
     * <p>
     * Requires the user to be logged in and have the MO role permission, otherwise access is blocked.
     * </p>
     *
     * @param req  HTTP request
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during forwarding or redirect
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        if (!requirePermission(req, resp, User.Role.MO)) return;

        forwardTo(req, resp, "/secure/mo/post_position.jsp");
    }

    /**
     * Handles POST requests to create a new position.
     * <p>
     * Receives title, moduleCode, workload, requirements, and deadline parameters.
     * Validates that required fields are not empty, parses and checks that the deadline is a future date.
     * Upon successful validation, standardizes the position information (trims whitespace, uppercases
     * the module code) and saves it.
     * </p>
     *
     * @param req  HTTP request containing title, moduleCode, workload, requirements, deadline parameters
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during forwarding or redirect
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
