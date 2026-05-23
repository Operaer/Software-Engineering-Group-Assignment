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
 * TA Quick Apply Servlet.
 *
 * <p>Handles only POST requests. TAs submit a quick application for a position through
 * this Servlet. Before submitting, it verifies that the TA profile is complete (name,
 * student ID, major, phone, and resume must all be filled in) and that the position is
 * still open. It also prevents duplicate applications for the same position.</p>
 */
@WebServlet(name = "QuickApplyServlet", urlPatterns = "/secure/ta/quick-apply")
public class QuickApplyServlet extends BaseServlet {

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

    /**
     * Checks whether the TA profile is complete (name, student ID, major, phone, and resume must all be filled in).
     *
     * @param profile the TA profile object
     * @return true if all required fields are filled in
     */
    private boolean isProfileComplete(TAProfile profile) {
        return profile.getName() != null && !profile.getName().isBlank()
                && profile.getStudentId() != null && !profile.getStudentId().isBlank()
                && profile.getMajor() != null && !profile.getMajor().isBlank()
                && profile.getPhone() != null && !profile.getPhone().isBlank()
                && profile.getResumeFileName() != null && !profile.getResumeFileName().isBlank();
    }
}
