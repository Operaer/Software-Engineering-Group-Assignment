package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.ApplicationStorage;
import com.bupt.ta.storage.JobStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "ApplicationServlet", urlPatterns = "/secure/ta/applications")
public class ApplicationServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requireLogin(req, resp);

        User user = getCurrentUser(req);
        ApplicationStorage appStorage = new ApplicationStorage(getServletContext());
        JobStorage jobStorage = new JobStorage(getServletContext());
        
        List<Application> applications = appStorage.findByTaEmail(user.getEmail());
        List<Job> availableJobs = jobStorage.findAll().stream()
            .filter(job -> job.getDeadline().isAfter(LocalDate.now()))
            .filter(job -> Job.STATUS_OPEN.equals(job.getStatus()))
            .collect(Collectors.toList());

        req.setAttribute("applications", applications);
        req.setAttribute("availableJobs", availableJobs);
        forwardTo(req, resp, "/secure/ta/applications.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requireLogin(req, resp);

        User user = getCurrentUser(req);
        String jobId = req.getParameter("jobId");

        if (jobId == null || jobId.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/secure/ta/applications");
            return;
        }

        JobStorage jobStorage = new JobStorage(getServletContext());
        Job job = jobStorage.findById(jobId);
        if (job == null || !Job.STATUS_OPEN.equals(job.getStatus()) || job.getDeadline().isBefore(LocalDate.now())) {
            req.setAttribute("error", "This position is no longer available.");
            doGet(req, resp);
            return;
        }

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        
        // Check if user has already applied for this position
        if (storage.hasApplied(user.getEmail(), jobId)) {
            req.setAttribute("error", "You have already applied for this position.");
            doGet(req, resp);
            return;
        }
        
        Application application = storage.createNew(user.getEmail(), jobId, job.getTitle());

        req.setAttribute("success", "Application submitted successfully: " + application.getPositionTitle());
        doGet(req, resp);
    }
}
