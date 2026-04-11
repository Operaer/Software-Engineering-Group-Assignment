package com.bupt.ta.servlet;

import com.bupt.ta.model.Job;
import com.bupt.ta.model.JobHistoryEntry;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.JobHistoryStorage;
import com.bupt.ta.storage.JobStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/secure/mo/manage-job")
public class ManageJobServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        if (!requirePermission(req, resp, User.Role.MO)) return;

        JobStorage jobStorage = new JobStorage(getServletContext());
        JobHistoryStorage historyStorage = new JobHistoryStorage(getServletContext());
        String action = req.getParameter("action");
        String jobId = req.getParameter("jobId");

        if ("edit".equals(action) && jobId != null) {
            Job job = jobStorage.findById(jobId);
            if (job == null) {
                resp.sendRedirect(req.getContextPath() + "/secure/mo/manage-job");
                return;
            }
            req.setAttribute("job", job);
            forwardTo(req, resp, "/secure/mo/edit_position.jsp");
            return;
        }

        if ("view".equals(action) && jobId != null) {
            Job job = jobStorage.findById(jobId);
            if (job == null) {
                resp.sendRedirect(req.getContextPath() + "/secure/mo/manage-job");
                return;
            }
            boolean canRollback = historyStorage.findLastSnapshot(jobId) != null;
            req.setAttribute("job", job);
            req.setAttribute("canRollback", canRollback);
            forwardTo(req, resp, "/secure/mo/view_position.jsp");
            return;
        }

        if ("history".equals(action) && jobId != null) {
            Job job = jobStorage.findById(jobId);
            if (job == null) {
                resp.sendRedirect(req.getContextPath() + "/secure/mo/manage-job");
                return;
            }
            List<JobHistoryEntry> history = historyStorage.findByJobId(jobId);
            req.setAttribute("job", job);
            req.setAttribute("history", history);
            forwardTo(req, resp, "/secure/mo/job_history.jsp");
            return;
        }

        req.setAttribute("jobs", jobStorage.findAll());
        forwardTo(req, resp, "/secure/mo/manage_positions.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        if (!requirePermission(req, resp, User.Role.MO)) return;

        String action = req.getParameter("action");
        String jobId = req.getParameter("jobId");
        if (jobId == null || jobId.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/secure/mo/manage-job");
            return;
        }

        JobStorage jobStorage = new JobStorage(getServletContext());
        JobHistoryStorage historyStorage = new JobHistoryStorage(getServletContext());
        User currentUser = getCurrentUser(req);
        Job job = jobStorage.findById(jobId);
        if (job == null) {
            req.setAttribute("error", "Position not found.");
            doGet(req, resp);
            return;
        }

        if ("archive".equals(action)) {
            Job previousState = cloneJob(job);
            if (!Job.STATUS_ARCHIVED.equals(job.getStatus())) {
                job.setStatus(Job.STATUS_ARCHIVED);
                job.setUpdatedAt(Instant.now());
                jobStorage.update(job);
                historyStorage.record(job.getId(), "Archive", "Position archived by " + currentUser.getEmail(), previousState, currentUser.getEmail());
            }
            resp.sendRedirect(req.getContextPath() + "/secure/mo/manage-job");
            return;
        }

        if ("rollback".equals(action)) {
            Job previousSnapshot = historyStorage.findLastSnapshot(jobId);
            if (previousSnapshot == null) {
                req.setAttribute("error", "No previous version available for rollback.");
                req.setAttribute("job", job);
                req.setAttribute("canRollback", false);
                forwardTo(req, resp, "/secure/mo/view_position.jsp");
                return;
            }
            Job currentState = cloneJob(job);
            previousSnapshot.setUpdatedAt(Instant.now());
            jobStorage.update(previousSnapshot);
            historyStorage.record(job.getId(), "Rollback", "Reverted to previous position version.", currentState, currentUser.getEmail());
            resp.sendRedirect(req.getContextPath() + "/secure/mo/manage-job?action=view&jobId=" + jobId);
            return;
        }

        if ("update".equals(action)) {
            Job snapshot = cloneJob(job);
            String title = req.getParameter("title");
            String moduleCode = req.getParameter("moduleCode");
            String workload = req.getParameter("workload");
            String requirements = req.getParameter("requirements");
            String deadlineStr = req.getParameter("deadline");

            if (title == null || title.trim().isEmpty() ||
                    moduleCode == null || moduleCode.trim().isEmpty() ||
                    workload == null || workload.trim().isEmpty() ||
                    requirements == null || requirements.trim().isEmpty() ||
                    deadlineStr == null || deadlineStr.trim().isEmpty()) {
                req.setAttribute("error", "All fields are required.");
                req.setAttribute("job", job);
                forwardTo(req, resp, "/secure/mo/edit_position.jsp");
                return;
            }

            LocalDate deadline;
            try {
                deadline = LocalDate.parse(deadlineStr);
                if (deadline.isBefore(LocalDate.now())) {
                    req.setAttribute("error", "Deadline must be a future date.");
                    req.setAttribute("job", job);
                    forwardTo(req, resp, "/secure/mo/edit_position.jsp");
                    return;
                }
            } catch (DateTimeParseException e) {
                req.setAttribute("error", "Invalid deadline format.");
                req.setAttribute("job", job);
                forwardTo(req, resp, "/secure/mo/edit_position.jsp");
                return;
            }

            StringBuilder changes = new StringBuilder();
            title = title.trim();
            moduleCode = moduleCode.trim().toUpperCase();
            workload = workload.trim();
            requirements = requirements.trim();

            if (!title.equals(job.getTitle())) {
                changes.append("Title: '" + job.getTitle() + "' → '" + title + "'; ");
                job.setTitle(title);
            }
            if (!moduleCode.equals(job.getModuleCode())) {
                changes.append("Module: '" + job.getModuleCode() + "' → '" + moduleCode + "'; ");
                job.setModuleCode(moduleCode);
            }
            if (!workload.equals(job.getWorkload())) {
                changes.append("Workload: '" + job.getWorkload() + "' → '" + workload + "'; ");
                job.setWorkload(workload);
            }
            if (!requirements.equals(job.getRequirements())) {
                changes.append("Requirements updated; ");
                job.setRequirements(requirements);
            }
            if (!deadline.equals(job.getDeadline())) {
                changes.append("Deadline: '" + job.getDeadline() + "' → '" + deadline + "'; ");
                job.setDeadline(deadline);
            }

            job.setUpdatedAt(Instant.now());
            jobStorage.update(job);
            String historyDetail = changes.length() > 0 ? changes.toString() : "No content changes. Metadata refreshed.";
            historyStorage.record(job.getId(), "Update", historyDetail, snapshot, currentUser.getEmail());

            req.setAttribute("success", "Position updated successfully.");
            req.setAttribute("job", job);
            forwardTo(req, resp, "/secure/mo/edit_position.jsp");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/secure/mo/manage-job");
    }

    private Job cloneJob(Job job) {
        Job clone = new Job();
        clone.setId(job.getId());
        clone.setTitle(job.getTitle());
        clone.setModuleCode(job.getModuleCode());
        clone.setWorkload(job.getWorkload());
        clone.setRequirements(job.getRequirements());
        clone.setDeadline(job.getDeadline());
        clone.setPostedBy(job.getPostedBy());
        clone.setPostedAt(job.getPostedAt());
        clone.setUpdatedAt(job.getUpdatedAt());
        clone.setStatus(job.getStatus());
        return clone;
    }
}

