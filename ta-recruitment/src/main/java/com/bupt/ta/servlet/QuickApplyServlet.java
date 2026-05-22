package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.TAProfile;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.ApplicationStorage;
import com.bupt.ta.storage.JobStorage;
import com.bupt.ta.storage.ProfileStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Provides a one-click apply action for TA positions.
 *
 * <p>Mapped to {@code /secure/ta/quick-apply}. Processes POST requests to submit
 * an application for a specific job. Checks that the TA's profile is complete
 * before applying; otherwise, redirects to the profile page with a return URL.
 * Also prevents duplicate applications for the same position.</p>
 */
@WebServlet(name = "QuickApplyServlet", urlPatterns = "/secure/ta/quick-apply")
public class QuickApplyServlet extends BaseServlet {

    /**
     * Processes a quick-apply request from a TA.
     *
     * <p>Verifies that the specified job is open and available, the TA's profile
     * is complete (name, student ID, major, phone, and resume are all present),
     * and the TA has not already applied. On success, creates the application
     * and redirects to the applications page with a success indicator.</p>
     *
     * @param req  the HTTP request containing {@code jobId} parameter
     * @param resp the HTTP response
     * @throws ServletException if an unexpected servlet error occurs
     * @throws IOException      if the redirect fails
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }

        User user = getCurrentUser(req);
        String jobId = req.getParameter("jobId");

        if (jobId == null || jobId.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/secure/ta/positions");
            return;
        }

        JobStorage jobStorage = new JobStorage(getServletContext());
        Job job = jobStorage.findById(jobId);
        if (job == null || !job.getStatus().equals(Job.STATUS_OPEN)) {
            req.setAttribute("error", "This position is no longer available.");
            resp.sendRedirect(req.getContextPath() + "/secure/ta/positions");
            return;
        }

        ProfileStorage profileStorage = new ProfileStorage(getServletContext());
        TAProfile profile = profileStorage.load(user.getEmail());

        if (profile == null || !isProfileComplete(profile)) {
            resp.sendRedirect(req.getContextPath() + "/secure/ta/profile?redirectAfterProfile=/secure/ta/positions");
            return;
        }

        ApplicationStorage appStorage = new ApplicationStorage(getServletContext());
        
        if (appStorage.hasApplied(user.getEmail(), jobId)) {
            resp.sendRedirect(req.getContextPath() + "/secure/ta/applications?alreadyApplied=true");
            return;
        }
        
        Application application = appStorage.createNew(user.getEmail(), jobId, job.getTitle());

        resp.sendRedirect(req.getContextPath() + "/secure/ta/applications?success=true&position=" + application.getPositionTitle());
    }

    private boolean isProfileComplete(TAProfile profile) {
        return profile.getName() != null && !profile.getName().isBlank()
                && profile.getStudentId() != null && !profile.getStudentId().isBlank()
                && profile.getMajor() != null && !profile.getMajor().isBlank()
                && profile.getPhone() != null && !profile.getPhone().isBlank()
                && profile.getResumeFileName() != null && !profile.getResumeFileName().isBlank();
    }
}