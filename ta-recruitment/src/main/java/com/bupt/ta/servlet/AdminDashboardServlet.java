package com.bupt.ta.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bupt.ta.model.AdminDashboardStats;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.ApplicationStorage;
import com.bupt.ta.storage.JobStorage;

/**
 * Admin Global Recruitment Dashboard Servlet (US03).
 *
 * <p>Provides school administrators with a centralized monitoring dashboard for the
 * overall TA recruitment process. It aggregates data from job storage and application
 * storage to generate institution-wide statistical indicators, module-level applicant
 * statistics, and workload distribution summaries.</p>
 *
 * <p>Supports filtering by module. When a specific module is selected, all statistics
 * are recalculated based solely on that module's positions and applications. Each request
 * reads the latest data from JSON storage, so MO status updates are reflected in the
 * admin dashboard upon refresh without needing an additional caching mechanism.</p>
 *
 * @author Wenqi Guan
 * @author Operaer
 * @version 1.0
 * @since 2026-05-09
 */
public class AdminDashboardServlet extends BaseServlet {
    private static final DateTimeFormatter SYNC_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private JobStorage jobStorage;
    private ApplicationStorage applicationStorage;

    @Override
    public void init() throws ServletException {
        this.jobStorage = new JobStorage(getServletContext());
        this.applicationStorage = new ApplicationStorage(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp) || !requirePermission(req, resp, User.Role.ADMIN)) {
            return;
        }

        // US03 AC4: always read the latest JSON storage on each request so that
        // MO status updates are reflected without maintaining a separate cache.
        disableDashboardCaching(resp);
        List<Job> allJobs = jobStorage.findAll();
        List<Application> allApplications = applicationStorage.findAll();

        String selectedModule = normalizeSelectedModule(req.getParameter("course"));
        String jobTitle = normalizeOptionalText(req.getParameter("jobTitle"));
        String createdBy = normalizeOptionalText(req.getParameter("createdBy"));
        LocalDate deadlineAfter = parseOptionalDate(req.getParameter("deadlineAfter"));
        LocalDate deadlineBefore = parseOptionalDate(req.getParameter("deadlineBefore"));
        String workload = normalizeOptionalText(req.getParameter("workload"));
        String status = normalizeOptionalText(req.getParameter("status"));
        List<String> moduleOptions = buildModuleOptions(allJobs);
        List<Job> filteredJobs = filterJobs(allJobs, selectedModule, jobTitle, createdBy, deadlineAfter, deadlineBefore, workload, status);
        Set<String> filteredJobIds = filteredJobs.stream()
                .map(Job::getId)
                .collect(Collectors.toSet());
        List<Application> filteredApplications = filterApplicationsByJobIds(allApplications, filteredJobIds);

        AdminDashboardStats stats = buildStats(filteredJobs, filteredApplications);

        req.setAttribute("stats", stats);
        req.setAttribute("moduleOptions", moduleOptions);
        req.setAttribute("selectedModule", selectedModule);
        req.setAttribute("jobTitle", jobTitle);
        req.setAttribute("createdBy", createdBy);
        req.setAttribute("deadlineAfter", deadlineAfter);
        req.setAttribute("deadlineBefore", deadlineBefore);
        req.setAttribute("workload", workload);
        req.setAttribute("status", status);
        req.setAttribute("filteredJobs", filteredJobs);
        req.setAttribute("lastSyncedAt", LocalDateTime.now().format(SYNC_TIME_FORMATTER));
        req.setAttribute("syncMessage", "Dashboard data is recalculated from the latest MO hiring decisions on every refresh.");
        forwardTo(req, resp, "/WEB-INF/secure/admin/global_dashboard.jsp");
    }

    /**
     * Disables HTTP caching for the dashboard page, ensuring the latest data is fetched on each request.
     *
     * @param resp the HTTP response object
     */
    private void disableDashboardCaching(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
    }

    /**
     * Normalizes the module filter parameter; returns "ALL" when null or "ALL".
     *
     * @param selectedModule the raw module parameter
     * @return the normalized module code or "ALL"
     */
    private String normalizeSelectedModule(String selectedModule) {
        if (selectedModule == null || selectedModule.isBlank() || "ALL".equalsIgnoreCase(selectedModule)) {
            return "ALL";
        }
        return selectedModule.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Extracts a deduplicated and sorted list of module codes from all jobs for the frontend filter dropdown.
     *
     * @param jobs all job listings
     * @return the sorted list of module codes
     */
    private List<String> buildModuleOptions(List<Job> jobs) {
        return jobs.stream()
                .map(job -> normalizeModuleCode(job.getModuleCode()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Applies multi-dimensional admin job filtering (AND semantics).
     *
     * @param jobs           all persisted jobs
     * @param selectedModule the selected course/module filter
     * @param jobTitle       optional title keyword
     * @param createdBy      optional MO creator keyword
     * @param deadlineAfter  optional deadline lower bound (inclusive)
     * @param deadlineBefore optional deadline upper bound (inclusive)
     * @param workload       optional workload keyword
     * @param status         optional effective status filter
     * @return the list of jobs matching all provided filter criteria
     */
    private List<Job> filterJobs(List<Job> jobs, String selectedModule, String jobTitle, String createdBy,
                                 LocalDate deadlineAfter, LocalDate deadlineBefore, String workload, String status) {
        return jobs.stream()
                .filter(job -> "ALL".equalsIgnoreCase(selectedModule)
                        || selectedModule.equals(normalizeModuleCode(job.getModuleCode())))
                .filter(job -> jobTitle == null || containsIgnoreCase(job.getTitle(), jobTitle))
                .filter(job -> createdBy == null || containsIgnoreCase(job.getPostedBy(), createdBy))
                .filter(job -> deadlineAfter == null || (job.getDeadline() != null && !job.getDeadline().isBefore(deadlineAfter)))
                .filter(job -> deadlineBefore == null || (job.getDeadline() != null && !job.getDeadline().isAfter(deadlineBefore)))
                .filter(job -> workload == null || containsIgnoreCase(job.getWorkload(), workload))
                .filter(job -> status == null || status.equalsIgnoreCase(getEffectiveStatus(job)))
                .collect(Collectors.toList());
    }

    /**
     * Filters the application list based on the set of visible job IDs.
     *
     * @param applications  all applications
     * @param visibleJobIds the set of visible job IDs
     * @return the filtered application list
     */
    private List<Application> filterApplicationsByJobIds(List<Application> applications, Set<String> visibleJobIds) {
        return applications.stream()
                .filter(application -> visibleJobIds.contains(application.getPositionId()))
                .collect(Collectors.toList());
    }

    /**
     * Builds admin dashboard statistics, including job statistics, application statistics,
     * module metrics, and TA workload metrics.
     *
     * @param jobs         the job list
     * @param applications the application list
     * @return the dashboard statistics object
     */
    private AdminDashboardStats buildStats(List<Job> jobs, List<Application> applications) {
        AdminDashboardStats stats = new AdminDashboardStats();
        stats.setTotalPositions(jobs.size());
        stats.setOpenPositions(countJobsByStatus(jobs, Job.STATUS_OPEN));
        stats.setClosedPositions(countJobsByStatus(jobs, Job.STATUS_CLOSED));
        stats.setArchivedPositions(countJobsByStatus(jobs, Job.STATUS_ARCHIVED));
        stats.setTotalApplications(applications.size());
        stats.setAcceptedApplications(countApplicationsByStatus(applications, Application.Status.Accepted.name()));
        stats.setCompletionRate(calculateRate(stats.getAcceptedApplications(), stats.getTotalApplications()));
        stats.setModuleMetrics(buildModuleMetrics(jobs, applications));
        stats.setTaWorkloadMetrics(buildTAWorkloadMetrics(jobs, applications));
        stats.setModuleWorkloadMetrics(buildModuleWorkloadMetrics(jobs, applications));
        return stats;
    }

    /**
     * Counts jobs matching a given status.
     *
     * @param jobs   the job list
     * @param status the target status
     * @return the count of jobs matching that status
     */
    private int countJobsByStatus(List<Job> jobs, String status) {
        return (int) jobs.stream()
                .filter(job -> equalsIgnoreCase(status, job.getStatus()))
                .count();
    }

    /**
     * Counts applications matching a given status.
     *
     * @param applications the application list
     * @param status       the target status
     * @return the count of applications matching that status
     */
    private int countApplicationsByStatus(List<Application> applications, String status) {
        return (int) applications.stream()
                .filter(application -> equalsIgnoreCase(status, application.getStatus()))
                .count();
    }

    /**
     * Builds per-module statistics (position count, applicant count, accepted count, completion rate).
     *
     * @param jobs         the job list
     * @param applications the application list
     * @return the list of module metrics
     */
    private List<AdminDashboardStats.ModuleMetric> buildModuleMetrics(List<Job> jobs, List<Application> applications) {
        Map<String, Job> jobsById = jobs.stream()
                .collect(Collectors.toMap(Job::getId, job -> job, (first, second) -> first));

        Map<String, AdminDashboardStats.ModuleMetric> metricsByModule = new HashMap<>();
        for (Job job : jobs) {
            String moduleCode = normalizeModuleCode(job.getModuleCode());
            AdminDashboardStats.ModuleMetric metric = metricsByModule.computeIfAbsent(moduleCode, this::newModuleMetric);
            metric.setPositionCount(metric.getPositionCount() + 1);
        }

        for (Application application : applications) {
            Job job = jobsById.get(application.getPositionId());
            String moduleCode = job == null ? "Unknown" : normalizeModuleCode(job.getModuleCode());
            AdminDashboardStats.ModuleMetric metric = metricsByModule.computeIfAbsent(moduleCode, this::newModuleMetric);
            metric.setApplicantCount(metric.getApplicantCount() + 1);
            if (equalsIgnoreCase(Application.Status.Accepted.name(), application.getStatus())) {
                metric.setAcceptedCount(metric.getAcceptedCount() + 1);
            }
        }

        List<AdminDashboardStats.ModuleMetric> metrics = new ArrayList<>(metricsByModule.values());
        for (AdminDashboardStats.ModuleMetric metric : metrics) {
            metric.setCompletionRate(calculateRate(metric.getAcceptedCount(), metric.getApplicantCount()));
        }
        metrics.sort(Comparator.comparing(AdminDashboardStats.ModuleMetric::getModuleCode));
        return metrics;
    }

    /**
     * Builds TA workload metrics, counting the number of positions, total workload, and
     * associated modules for each accepted TA.
     *
     * @param jobs         the job list
     * @param applications the application list
     * @return the list of TA workload metrics
     */
    private List<AdminDashboardStats.TAWorkloadMetric> buildTAWorkloadMetrics(List<Job> jobs, List<Application> applications) {
        Map<String, Job> jobsById = jobs.stream()
                .collect(Collectors.toMap(Job::getId, job -> job, (first, second) -> first));
        Map<String, TAWorkloadAccumulator> workloadByTA = new HashMap<>();
        Set<String> countedTAPositionPairs = new HashSet<>();

        for (Application application : applications) {
            if (!equalsIgnoreCase(Application.Status.Accepted.name(), application.getStatus())) {
                continue;
            }

            String taEmail = normalizeText(application.getTaEmail(), "Unknown TA");
            String positionId = normalizeText(application.getPositionId(), "Unknown Position");
            String uniqueKey = taEmail.toLowerCase(Locale.ROOT) + "|" + positionId;
            if (!countedTAPositionPairs.add(uniqueKey)) {
                continue;
            }

            Job job = jobsById.get(application.getPositionId());
            String moduleCode = job == null ? "Unknown" : normalizeModuleCode(job.getModuleCode());
            int workload = calculateEffectiveWorkload(application, job);

            TAWorkloadAccumulator accumulator = workloadByTA.computeIfAbsent(taEmail, key -> new TAWorkloadAccumulator());
            accumulator.acceptedPositions++;
            accumulator.totalWorkload += workload;
            accumulator.modules.add(moduleCode);
        }

        return workloadByTA.entrySet().stream()
                .map(entry -> toTAWorkloadMetric(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(AdminDashboardStats.TAWorkloadMetric::getTotalWorkload).reversed()
                        .thenComparing(AdminDashboardStats.TAWorkloadMetric::getTaEmail))
                .collect(Collectors.toList());
    }

    /**
     * Builds module workload metrics, counting accepted positions, total workload, and
     * hired TA count for each module.
     *
     * @param jobs         the job list
     * @param applications the application list
     * @return the list of module workload metrics
     */
    private List<AdminDashboardStats.ModuleWorkloadMetric> buildModuleWorkloadMetrics(List<Job> jobs, List<Application> applications) {
        Map<String, Job> jobsById = jobs.stream()
                .collect(Collectors.toMap(Job::getId, job -> job, (first, second) -> first));
        Map<String, ModuleWorkloadAccumulator> workloadByModule = new HashMap<>();
        Set<String> countedTAPositionPairs = new HashSet<>();

        for (Application application : applications) {
            if (!equalsIgnoreCase(Application.Status.Accepted.name(), application.getStatus())) {
                continue;
            }

            String taEmail = normalizeText(application.getTaEmail(), "Unknown TA");
            String positionId = normalizeText(application.getPositionId(), "Unknown Position");
            String uniqueKey = taEmail.toLowerCase(Locale.ROOT) + "|" + positionId;
            if (!countedTAPositionPairs.add(uniqueKey)) {
                continue;
            }

            Job job = jobsById.get(application.getPositionId());
            String moduleCode = job == null ? "Unknown" : normalizeModuleCode(job.getModuleCode());
            int workload = calculateEffectiveWorkload(application, job);

            ModuleWorkloadAccumulator accumulator = workloadByModule.computeIfAbsent(moduleCode, key -> new ModuleWorkloadAccumulator());
            accumulator.acceptedPositions++;
            accumulator.totalWorkload += workload;
            accumulator.hiredTAs.add(taEmail);
        }

        return workloadByModule.entrySet().stream()
                .map(entry -> toModuleWorkloadMetric(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(AdminDashboardStats.ModuleWorkloadMetric::getModuleCode))
                .collect(Collectors.toList());
    }

    /**
     * Converts a TA accumulator to a TA workload metric object.
     *
     * @param taEmail     the TA email
     * @param accumulator the TA workload accumulator
     * @return the TA workload metric
     */
    private AdminDashboardStats.TAWorkloadMetric toTAWorkloadMetric(String taEmail, TAWorkloadAccumulator accumulator) {
        AdminDashboardStats.TAWorkloadMetric metric = new AdminDashboardStats.TAWorkloadMetric();
        metric.setTaEmail(taEmail);
        metric.setAcceptedPositions(accumulator.acceptedPositions);
        metric.setTotalWorkload(accumulator.totalWorkload);
        metric.setModules(String.join(", ", accumulator.modules));
        return metric;
    }

    /**
     * Converts a module accumulator to a module workload metric object.
     *
     * @param moduleCode  the module code
     * @param accumulator the module workload accumulator
     * @return the module workload metric
     */
    private AdminDashboardStats.ModuleWorkloadMetric toModuleWorkloadMetric(String moduleCode, ModuleWorkloadAccumulator accumulator) {
        AdminDashboardStats.ModuleWorkloadMetric metric = new AdminDashboardStats.ModuleWorkloadMetric();
        metric.setModuleCode(moduleCode);
        metric.setHiredTAs(accumulator.hiredTAs.size());
        metric.setAcceptedPositions(accumulator.acceptedPositions);
        metric.setTotalWorkload(accumulator.totalWorkload);
        return metric;
    }

    /**
     * Parses the numeric part from a workload description string.
     *
     * @param workload the workload description string (e.g., "20 hours/week")
     * @return the parsed workload value, or 0 if parsing fails
     */
    private int parseWorkload(String workload) {
        if (workload == null || workload.isBlank()) {
            return 0;
        }
        Matcher matcher = Pattern.compile("\\d+").matcher(workload);
        if (!matcher.find()) {
            return 0;
        }
        try {
            return Integer.parseInt(matcher.group());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Calculates the effective workload for an application. Uses the application-specific
     * assigned workload if available; otherwise parses the workload from the job description.
     *
     * @param application the application object
     * @param job         the job object
     * @return the effective workload
     */
    private int calculateEffectiveWorkload(Application application, Job job) {
        if (application == null) {
            return 0;
        }
        Integer assigned = application.getAssignedWorkloadHours();
        if (assigned != null) {
            return assigned;
        }
        return parseWorkload(job == null ? null : job.getWorkload());
    }

    /**
     * Normalizes text: trims whitespace, returns fallback for null/blank values.
     *
     * @param value    the raw text
     * @param fallback the default value
     * @return the normalized text
     */
    private String normalizeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    /**
     * Normalizes optional text: trims whitespace, returns null for null/blank values.
     *
     * @param value the raw text
     * @return the normalized text, or null if blank
     */
    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Parses an optional date parameter, returns null for invalid formats.
     *
     * @param value the date string
     * @return the parsed LocalDate, or null if parsing fails
     */
    private LocalDate parseOptionalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Checks whether a string contains a keyword, ignoring case.
     *
     * @param value   the target string
     * @param keyword the keyword
     * @return true if the target contains the keyword
     */
    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && keyword != null && value.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    /**
     * Gets the effective status of a job. Archived jobs return archived status;
     * jobs past their deadline are considered closed; otherwise the actual status is returned.
     *
     * @param job the job object
     * @return the effective status string
     */
    private String getEffectiveStatus(Job job) {
        if (job == null) {
            return "Unknown";
        }
        if (Job.STATUS_ARCHIVED.equalsIgnoreCase(job.getStatus())) {
            return Job.STATUS_ARCHIVED;
        }
        if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDate.now())) {
            return Job.STATUS_CLOSED;
        }
        return job.getStatus();
    }

    /**
     * TA workload accumulator, used to count the number of accepted positions, total workload,
     * and associated module set for each TA.
     */
    private static class TAWorkloadAccumulator {
        private int acceptedPositions;
        private int totalWorkload;
        private final Set<String> modules = new TreeSet<>();
    }

    /**
     * Module workload accumulator, used to count the number of accepted positions, total workload,
     * and hired TA set for each module.
     */
    private static class ModuleWorkloadAccumulator {
        private int acceptedPositions;
        private int totalWorkload;
        private final Set<String> hiredTAs = new HashSet<>();
    }

    /**
     * Creates a new module metric object and sets its module code.
     *
     * @param moduleCode the module code
     * @return a new module metric object
     */
    private AdminDashboardStats.ModuleMetric newModuleMetric(String moduleCode) {
        AdminDashboardStats.ModuleMetric metric = new AdminDashboardStats.ModuleMetric();
        metric.setModuleCode(moduleCode);
        return metric;
    }

    /**
     * Normalizes a module code: trims whitespace and converts to uppercase; returns "Unknown" for blank values.
     *
     * @param moduleCode the raw module code
     * @return the normalized module code
     */
    private String normalizeModuleCode(String moduleCode) {
        if (moduleCode == null || moduleCode.isBlank()) {
            return "Unknown";
        }
        return moduleCode.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Calculates the completion rate (completed / total * 100).
     *
     * @param completed the completed count
     * @param total     the total count
     * @return the completion rate as a percentage
     */
    private double calculateRate(int completed, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return completed * 100.0 / total;
    }

    /**
     * Compares two strings for equality, ignoring case.
     *
     * @param expected the expected value
     * @param actual   the actual value
     * @return true if they are equal ignoring case
     */
    private boolean equalsIgnoreCase(String expected, String actual) {
        return expected != null && actual != null && expected.equalsIgnoreCase(actual);
    }
}
