package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.MOProgressStats;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.ApplicationStorage;
import com.bupt.ta.storage.JobStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the MO05 Recruitment Progress Dashboard.
 *
 * <p>The servlet calculates recruitment progress dynamically from the current
 * job and application records. It provides per-position applicant, shortlisted,
 * accepted, deadline countdown, and colour status data to the progress JSP.</p>
 *
 * @author Wenqi Guan
 * @version 1.0
 * @since 2026-05-21
 */
@WebServlet(name = "MOProgressServlet", urlPatterns = "/secure/mo/progress")
public class MOProgressServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }
        if (!requirePermission(req, resp, User.Role.MO)) {
            return;
        }

        User currentUser = getCurrentUser(req);
        JobStorage jobStorage = new JobStorage(getServletContext());
        ApplicationStorage applicationStorage = new ApplicationStorage(getServletContext());

        List<Job> jobs = filterJobsForCurrentMO(jobStorage.findAll(), currentUser);
        List<Application> applications = applicationStorage.findAll();
        MOProgressStats stats = buildStats(jobs, applications);

        req.setAttribute("stats", stats);
        req.setAttribute("currentUser", currentUser);
        forwardTo(req, resp, "/secure/mo/progress.jsp");
    }

    /**
     * Keeps the dashboard focused on the current MO's postings when postedBy is available.
     * Older seed data may not contain postedBy consistently, so those records remain visible.
     *
     * @param jobs all jobs from storage
     * @param currentUser currently logged-in MO
     * @return jobs relevant to the current MO
     */
    private List<Job> filterJobsForCurrentMO(List<Job> jobs, User currentUser) {
        if (currentUser == null || currentUser.getEmail() == null) {
            return jobs;
        }
        String currentEmail = currentUser.getEmail().toLowerCase();
        return jobs.stream()
                .filter(job -> job.getPostedBy() == null
                        || job.getPostedBy().isBlank()
                        || currentEmail.equals(job.getPostedBy().toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Builds all summary cards and per-position progress rows.
     *
     * @param jobs MO position records
     * @param applications all application records
     * @return calculated dashboard statistics
     */
    private MOProgressStats buildStats(List<Job> jobs, List<Application> applications) {
        MOProgressStats stats = new MOProgressStats();
        LocalDate today = LocalDate.now();
        Map<String, List<Application>> applicationsByPosition = groupApplicationsByPosition(applications);
        List<MOProgressStats.PositionProgress> progressRows = new ArrayList<>();

        for (Job job : jobs) {
            List<Application> jobApplications = applicationsByPosition.getOrDefault(job.getId(), new ArrayList<>());
            MOProgressStats.PositionProgress row = buildPositionProgress(job, jobApplications, today);
            progressRows.add(row);

            stats.setTotalApplicants(stats.getTotalApplicants() + row.getApplicantCount());
            stats.setTotalShortlisted(stats.getTotalShortlisted() + row.getShortlistedCount());
            stats.setTotalAccepted(stats.getTotalAccepted() + row.getAcceptedCount());
            if ("Completed".equals(row.getProgressLabel())) {
                stats.setCompletedPositions(stats.getCompletedPositions() + 1);
            } else {
                stats.setInProgressPositions(stats.getInProgressPositions() + 1);
            }
            if (row.getDaysRemaining() >= 0 && row.getDaysRemaining() <= 3 && row.getAcceptedCount() == 0) {
                stats.setUrgentPositions(stats.getUrgentPositions() + 1);
            }
        }

        progressRows.sort(Comparator
                .comparing(MOProgressStats.PositionProgress::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(MOProgressStats.PositionProgress::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)));
        stats.setTotalPositions(progressRows.size());
        stats.setPositions(progressRows);
        return stats;
    }

    private Map<String, List<Application>> groupApplicationsByPosition(List<Application> applications) {
        Map<String, List<Application>> grouped = new HashMap<>();
        for (Application application : applications) {
            if (application.getPositionId() == null) {
                continue;
            }
            grouped.computeIfAbsent(application.getPositionId(), key -> new ArrayList<>()).add(application);
        }
        return grouped;
    }

    private MOProgressStats.PositionProgress buildPositionProgress(Job job, List<Application> applications, LocalDate today) {
        MOProgressStats.PositionProgress row = new MOProgressStats.PositionProgress();
        int shortlisted = countByStatus(applications, Application.Status.Shortlisted.name());
        int accepted = countByStatus(applications, Application.Status.Accepted.name());
        long daysRemaining = job.getDeadline() == null ? 0 : ChronoUnit.DAYS.between(today, job.getDeadline());

        row.setJobId(job.getId());
        row.setTitle(job.getTitle());
        row.setModuleCode(job.getModuleCode());
        row.setJobStatus(job.getStatus());
        row.setDeadline(job.getDeadline());
        row.setDaysRemaining(daysRemaining);
        row.setApplicantCount(applications.size());
        row.setShortlistedCount(shortlisted);
        row.setAcceptedCount(accepted);
        row.setProgressPercent(calculateProgressPercent(applications.size(), shortlisted, accepted));
        applyProgressStatus(row, job, daysRemaining);
        return row;
    }

    private int countByStatus(List<Application> applications, String status) {
        int count = 0;
        for (Application application : applications) {
            if (status.equalsIgnoreCase(application.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private int calculateProgressPercent(int applicants, int shortlisted, int accepted) {
        if (accepted > 0) {
            return 100;
        }
        if (shortlisted > 0 && applicants > 0) {
            return 70;
        }
        if (applicants > 0) {
            return 35;
        }
        return 10;
    }

    private void applyProgressStatus(MOProgressStats.PositionProgress row, Job job, long daysRemaining) {
        if (Job.STATUS_ARCHIVED.equalsIgnoreCase(job.getStatus()) || Job.STATUS_CLOSED.equalsIgnoreCase(job.getStatus()) || row.getAcceptedCount() > 0) {
            row.setProgressLabel("Completed");
            row.setBadgeClass("bg-success");
        } else if (daysRemaining < 0) {
            row.setProgressLabel("Overdue");
            row.setBadgeClass("bg-danger");
        } else if (row.getApplicantCount() == 0) {
            row.setProgressLabel("No Applicants");
            row.setBadgeClass("bg-secondary");
        } else if (daysRemaining <= 3) {
            row.setProgressLabel("Urgent");
            row.setBadgeClass("bg-warning text-dark");
        } else if (row.getShortlistedCount() > 0) {
            row.setProgressLabel("Shortlisting");
            row.setBadgeClass("bg-info text-dark");
        } else {
            row.setProgressLabel("In Progress");
            row.setBadgeClass("bg-primary");
        }
    }
}
