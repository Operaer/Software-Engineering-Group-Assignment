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
 * Servlet for the US03 Admin Global Recruitment Dashboard.
 *
 * <p>
 * This servlet provides a centralized dashboard for school administrators to
 * monitor the overall TA recruitment process. It aggregates data from job
 * storage and application storage, then generates school-wide metrics,
 * module-level applicant statistics, and workload distribution summaries.
 * </p>
 *
 * <p>
 * The dashboard supports filtering by module. When a module is selected, all
 * statistics are recalculated based only on the jobs and applications belonging
 * to that module.
 * </p>
 *
 * <p>
 * The servlet reads the latest JSON storage data on every request. Therefore,
 * when a Module Organizer marks an application as accepted, the admin dashboard
 * reflects the updated hiring result after refresh without using a separate
 * cache.
 * </p>
 *
 * @author Wenqi Guan
 * @author Operaer
 * @date 2026-05-17
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
     * Prevents the browser from showing stale dashboard values after an MO
     * updates an application status to Accepted.
     */
    private void disableDashboardCaching(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
    }


    /**
     * Normalizes the module filter value submitted from the dashboard form.
     *
     * <p>An empty value or "ALL" means that the Admin wants to view all modules.
     * Otherwise the value is normalized to uppercase so it can be compared with
     * module codes stored in different letter cases.</p>
     */
    private String normalizeSelectedModule(String selectedModule) {
        if (selectedModule == null || selectedModule.isBlank() || "ALL".equalsIgnoreCase(selectedModule)) {
            return "ALL";
        }
        return selectedModule.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Builds the available module choices for the filter dropdown.
     */
    private List<String> buildModuleOptions(List<Job> jobs) {
        return jobs.stream()
                .map(job -> normalizeModuleCode(job.getModuleCode()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Applies the admin multi-dimensional position filters using AND semantics.
     *
     * @param jobs all persisted positions
     * @param selectedModule selected course/module filter
     * @param jobTitle optional title keyword
     * @param createdBy optional MO creator keyword
     * @param deadlineAfter optional inclusive lower deadline bound
     * @param deadlineBefore optional inclusive upper deadline bound
     * @param workload optional workload keyword
     * @param status optional effective-status filter
     * @return positions matching every provided filter
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
     * Keeps only applications belonging to the jobs currently visible in the dashboard.
     */
    private List<Application> filterApplicationsByJobIds(List<Application> applications, Set<String> visibleJobIds) {
        return applications.stream()
                .filter(application -> visibleJobIds.contains(application.getPositionId()))
                .collect(Collectors.toList());
    }

    /**
     * Aggregates school-wide recruitment metrics from current job and application data.
     *
     * @param jobs all TA positions in the system
     * @param applications all applications submitted by TAs
     * @return dashboard statistics for the Admin view
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

    private int countJobsByStatus(List<Job> jobs, String status) {
        return (int) jobs.stream()
                .filter(job -> equalsIgnoreCase(status, job.getStatus()))
                .count();
    }

    private int countApplicationsByStatus(List<Application> applications, String status) {
        return (int) applications.stream()
                .filter(application -> equalsIgnoreCase(status, application.getStatus()))
                .count();
    }

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
     * Builds the per-TA workload distribution required by US03.
     *
     * <p>Only accepted applications are counted because they represent TAs who
     * have actually been hired. Duplicate accepted records for the same TA and
     * the same position are ignored to avoid double-counting test data.</p>
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
     * Builds the per-module workload distribution required by US03.
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

    private AdminDashboardStats.TAWorkloadMetric toTAWorkloadMetric(String taEmail, TAWorkloadAccumulator accumulator) {
        AdminDashboardStats.TAWorkloadMetric metric = new AdminDashboardStats.TAWorkloadMetric();
        metric.setTaEmail(taEmail);
        metric.setAcceptedPositions(accumulator.acceptedPositions);
        metric.setTotalWorkload(accumulator.totalWorkload);
        metric.setModules(String.join(", ", accumulator.modules));
        return metric;
    }

    private AdminDashboardStats.ModuleWorkloadMetric toModuleWorkloadMetric(String moduleCode, ModuleWorkloadAccumulator accumulator) {
        AdminDashboardStats.ModuleWorkloadMetric metric = new AdminDashboardStats.ModuleWorkloadMetric();
        metric.setModuleCode(moduleCode);
        metric.setHiredTAs(accumulator.hiredTAs.size());
        metric.setAcceptedPositions(accumulator.acceptedPositions);
        metric.setTotalWorkload(accumulator.totalWorkload);
        return metric;
    }

    /**
     * Extracts the first integer from the workload field.
     *
     * <p>This supports both simple values such as "24" and descriptive values
     * such as "10 hours per week".</p>
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

    private String normalizeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    /**
     * Normalizes an optional text filter from the admin dashboard.
     *
     * @param value raw form value
     * @return trimmed value, or {@code null} when blank
     */
    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Parses an optional admin date-filter field without failing the request.
     *
     * @param value raw form value
     * @return parsed date, or {@code null} when absent or invalid
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
     * Performs a null-safe, case-insensitive substring check for admin filters.
     *
     * @param value source text
     * @param keyword keyword to search for
     * @return {@code true} when the source contains the keyword
     */
    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && keyword != null && value.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the effective display status used by admin filters.
     *
     * <p>Expired non-archived jobs are treated as closed even if their stored
     * status is still open.</p>
     *
     * @param job position to inspect
     * @return effective position status
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

    private static class TAWorkloadAccumulator {
        private int acceptedPositions;
        private int totalWorkload;
        private final Set<String> modules = new TreeSet<>();
    }

    private static class ModuleWorkloadAccumulator {
        private int acceptedPositions;
        private int totalWorkload;
        private final Set<String> hiredTAs = new HashSet<>();
    }

    private AdminDashboardStats.ModuleMetric newModuleMetric(String moduleCode) {
        AdminDashboardStats.ModuleMetric metric = new AdminDashboardStats.ModuleMetric();
        metric.setModuleCode(moduleCode);
        return metric;
    }

    private String normalizeModuleCode(String moduleCode) {
        if (moduleCode == null || moduleCode.isBlank()) {
            return "Unknown";
        }
        return moduleCode.trim().toUpperCase(Locale.ROOT);
    }

    private double calculateRate(int completed, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return completed * 100.0 / total;
    }

    private boolean equalsIgnoreCase(String expected, String actual) {
        return expected != null && actual != null && expected.equalsIgnoreCase(actual);
    }
}
