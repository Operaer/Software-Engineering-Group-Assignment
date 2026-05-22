package com.bupt.ta.servlet;

import java.io.IOException;
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
import com.bupt.ta.storage.UserStorage;

/**
 * Servlet that provides workload management capabilities for School Administrators.
 *
 * <p>The servlet displays TA workload summaries, identifies overloaded TAs,
 * shows module workload distribution, and supports manual adjustment of assigned
 * workload hours for accepted TA applications.</p>
 *
 * @author Operaer
 * @date 2026-05-17
 */
public class AdminWorkloadServlet extends BaseServlet {
    private static final int DEFAULT_SAFE_WEEKLY_LIMIT = 20;

    private JobStorage jobStorage;
    private ApplicationStorage applicationStorage;
    private UserStorage userStorage;

    @Override
    public void init() throws ServletException {
        this.jobStorage = new JobStorage(getServletContext());
        this.applicationStorage = new ApplicationStorage(getServletContext());
        this.userStorage = new UserStorage(getServletContext());
    }

    /**
     * Handles GET requests to display the workload management dashboard.
     *
     * <p>Reads all jobs, applications, and user data, filters for accepted
     * applications, and computes per-TA and per-module workload summaries.
     * Supports filtering by TA name/email, module, and workload range.</p>
     *
     * @param req the HTTP request containing filter parameters
     * @param resp the HTTP response
     * @throws ServletException if forwarding to the JSP fails
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp) || !requirePermission(req, resp, User.Role.ADMIN)) {
            return;
        }

        String taFilter = normalizeOptionalText(req.getParameter("taFilter"));
        String moduleFilter = normalizeSelectedModule(req.getParameter("module"));
        Integer minWorkload = parseOptionalInteger(req.getParameter("minWorkload"));
        Integer maxWorkload = parseOptionalInteger(req.getParameter("maxWorkload"));
        int safeLimit = parseOptionalInteger(req.getParameter("safeLimit"), DEFAULT_SAFE_WEEKLY_LIMIT);

        List<Job> allJobs = jobStorage.findAll();
        List<Application> allApplications = applicationStorage.findAll();
        Map<String, Job> jobsById = allJobs.stream()
                .collect(Collectors.toMap(Job::getId, job -> job, (first, second) -> first));

        List<Application> acceptedApplications = allApplications.stream()
                .filter(application -> equalsIgnoreCase(Application.Status.Accepted.name(), application.getStatus()))
                .collect(Collectors.toList());

        List<String> moduleOptions = buildModuleOptions(allJobs);
        Map<String, String> userDisplayNames = buildUserNameIndex();

        List<AssignmentEntry> assignments = new ArrayList<>();
        Map<String, TAAccumulator> taByEmail = new HashMap<>();
        Map<String, ModuleAccumulator> moduleByCode = new HashMap<>();
        Set<String> countedAssignmentKeys = new HashSet<>();

        for (Application application : acceptedApplications) {
            String positionId = normalizeText(application.getPositionId(), "Unknown");
            Job job = jobsById.get(positionId);
            String moduleCode = normalizeModuleCode(job == null ? null : job.getModuleCode());
            if (!"ALL".equals(moduleFilter) && !moduleFilter.equalsIgnoreCase(moduleCode)) {
                continue;
            }

            int effectiveWorkload = calculateEffectiveWorkload(application, job);
            AssignmentEntry entry = new AssignmentEntry();
            entry.setApplicationId(application.getId());
            entry.setTaEmail(normalizeText(application.getTaEmail(), "Unknown TA"));
            entry.setTaName(userDisplayNames.getOrDefault(entry.getTaEmail().toLowerCase(Locale.ROOT), "Unknown"));
            entry.setPositionTitle(normalizeText(application.getPositionTitle(), "Unknown Position"));
            entry.setModuleCode(moduleCode);
            entry.setJobWorkload(parseWorkload(job == null ? null : job.getWorkload()));
            entry.setAssignedWorkload(application.getAssignedWorkloadHours());
            entry.setEffectiveWorkload(effectiveWorkload);
            assignments.add(entry);

            String uniqueKey = entry.getTaEmail().toLowerCase(Locale.ROOT) + "|" + positionId;
            if (!countedAssignmentKeys.add(uniqueKey)) {
                continue;
            }

            TAAccumulator taAccumulator = taByEmail.computeIfAbsent(entry.getTaEmail(), key -> new TAAccumulator(entry.getTaEmail(), entry.getTaName()));
            taAccumulator.acceptedPositions++;
            taAccumulator.totalWorkload += effectiveWorkload;
            taAccumulator.modules.add(moduleCode);

            ModuleAccumulator moduleAccumulator = moduleByCode.computeIfAbsent(moduleCode, key -> new ModuleAccumulator(moduleCode));
            moduleAccumulator.acceptedPositions++;
            moduleAccumulator.totalWorkload += effectiveWorkload;
            moduleAccumulator.hiredTAs.add(entry.getTaEmail());
        }

        List<AdminDashboardStats.TAWorkloadMetric> taMetrics = taByEmail.values().stream()
                .map(this::toTAWorkloadMetric)
                .filter(metric -> passesTAFilters(metric, taFilter, minWorkload, maxWorkload))
                .sorted(Comparator.comparing(AdminDashboardStats.TAWorkloadMetric::getTotalWorkload).reversed()
                        .thenComparing(AdminDashboardStats.TAWorkloadMetric::getTaEmail))
                .collect(Collectors.toList());

        List<AdminDashboardStats.ModuleWorkloadMetric> moduleMetrics = moduleByCode.values().stream()
                .map(this::toModuleWorkloadMetric)
                .sorted(Comparator.comparing(AdminDashboardStats.ModuleWorkloadMetric::getModuleCode))
                .collect(Collectors.toList());

        req.setAttribute("assignments", assignments);
        req.setAttribute("taMetrics", taMetrics);
        req.setAttribute("moduleMetrics", moduleMetrics);
        req.setAttribute("moduleOptions", moduleOptions);
        req.setAttribute("selectedModule", moduleFilter);
        req.setAttribute("taFilter", taFilter);
        req.setAttribute("minWorkload", minWorkload != null ? minWorkload : "");
        req.setAttribute("maxWorkload", maxWorkload != null ? maxWorkload : "");
        req.setAttribute("safeLimit", safeLimit);

        String successMessage = (String) req.getSession().getAttribute("adminWorkloadSuccess");
        if (successMessage != null) {
            req.setAttribute("successMessage", successMessage);
            req.getSession().removeAttribute("adminWorkloadSuccess");
        }

        forwardTo(req, resp, "/WEB-INF/secure/admin/workload_management.jsp");
    }

    /**
     * Handles POST requests to update assigned workload hours for an application.
     *
     * <p>Accepts an {@code applicationId} and {@code assignedWorkloadHours}
     * parameter. Validates that the workload is a non-negative integer,
     * updates the application record, and stores a success message in the
     * session for display on the subsequent GET request.</p>
     *
     * @param req the HTTP request containing applicationId and assignedWorkloadHours
     * @param resp the HTTP response
     * @throws ServletException if forwarding to the JSP fails
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp) || !requirePermission(req, resp, User.Role.ADMIN)) {
            return;
        }

        String applicationId = req.getParameter("applicationId");
        String hoursValue = req.getParameter("assignedWorkloadHours");
        Integer assignedHours = null;
        if (hoursValue != null && !hoursValue.trim().isEmpty()) {
            try {
                assignedHours = Integer.parseInt(hoursValue.trim());
                if (assignedHours < 0) {
                    throw new NumberFormatException("Negative workload not allowed");
                }
            } catch (NumberFormatException e) {
                req.setAttribute("error", "Assigned workload must be a non-negative integer.");
                doGet(req, resp);
                return;
            }
        }

        applicationStorage.updateAssignedWorkload(applicationId, assignedHours, getCurrentUser(req).getEmail());
        req.getSession().setAttribute("adminWorkloadSuccess", "Workload assignment updated successfully.");
        resp.sendRedirect(req.getContextPath() + "/secure/admin/workload-management");
    }

    private Map<String, String> buildUserNameIndex() throws IOException {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, User> entry : userStorage.getAllUsers().entrySet()) {
            User user = entry.getValue();
            if (user != null) {
                result.put(entry.getKey().toLowerCase(Locale.ROOT), normalizeText(user.getUsername(), entry.getKey()));
            }
        }
        return result;
    }

    private boolean passesTAFilters(AdminDashboardStats.TAWorkloadMetric metric, String taFilter, Integer minWorkload, Integer maxWorkload) {
        if (taFilter != null && !taFilter.isBlank()) {
            String normalized = taFilter.toLowerCase(Locale.ROOT);
            boolean matchesName = metric.getTaName() != null && metric.getTaName().toLowerCase(Locale.ROOT).contains(normalized);
            boolean matchesEmail = metric.getTaEmail() != null && metric.getTaEmail().toLowerCase(Locale.ROOT).contains(normalized);
            if (!matchesName && !matchesEmail) {
                return false;
            }
        }
        if (minWorkload != null && metric.getTotalWorkload() < minWorkload) {
            return false;
        }
        if (maxWorkload != null && metric.getTotalWorkload() > maxWorkload) {
            return false;
        }
        return true;
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

    private AdminDashboardStats.TAWorkloadMetric toTAWorkloadMetric(TAAccumulator accumulator) {
        AdminDashboardStats.TAWorkloadMetric metric = new AdminDashboardStats.TAWorkloadMetric();
        metric.setTaEmail(accumulator.taEmail);
        metric.setTaName(accumulator.taName);
        metric.setAcceptedPositions(accumulator.acceptedPositions);
        metric.setTotalWorkload(accumulator.totalWorkload);
        metric.setModules(String.join(", ", new TreeSet<>(accumulator.modules)));
        return metric;
    }

    private AdminDashboardStats.ModuleWorkloadMetric toModuleWorkloadMetric(ModuleAccumulator accumulator) {
        AdminDashboardStats.ModuleWorkloadMetric metric = new AdminDashboardStats.ModuleWorkloadMetric();
        metric.setModuleCode(accumulator.moduleCode);
        metric.setHiredTAs(accumulator.hiredTAs.size());
        metric.setAcceptedPositions(accumulator.acceptedPositions);
        metric.setTotalWorkload(accumulator.totalWorkload);
        return metric;
    }

    private List<String> buildModuleOptions(List<Job> jobs) {
        return jobs.stream()
                .map(job -> normalizeModuleCode(job.getModuleCode()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private String normalizeSelectedModule(String selectedModule) {
        if (selectedModule == null || selectedModule.isBlank() || "ALL".equalsIgnoreCase(selectedModule)) {
            return "ALL";
        }
        return selectedModule.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeModuleCode(String moduleCode) {
        if (moduleCode == null || moduleCode.isBlank()) {
            return "Unknown";
        }
        return moduleCode.trim().toUpperCase(Locale.ROOT);
    }

    private Integer parseOptionalInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseOptionalInteger(String value, int fallback) {
        Integer parsed = parseOptionalInteger(value);
        return parsed != null ? parsed : fallback;
    }

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

    private String normalizeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private boolean equalsIgnoreCase(String expected, String actual) {
        return expected != null && actual != null && expected.equalsIgnoreCase(actual);
    }

    private static class TAAccumulator {
        private final String taEmail;
        private final String taName;
        private int acceptedPositions;
        private int totalWorkload;
        private final Set<String> modules = new TreeSet<>();

        private TAAccumulator(String taEmail, String taName) {
            this.taEmail = taEmail;
            this.taName = taName;
        }
    }

    private static class ModuleAccumulator {
        private final String moduleCode;
        private int acceptedPositions;
        private int totalWorkload;
        private final Set<String> hiredTAs = new HashSet<>();

        private ModuleAccumulator(String moduleCode) {
            this.moduleCode = moduleCode;
        }
    }

    public static class AssignmentEntry {
        private String applicationId;
        private String taEmail;
        private String taName;
        private String positionTitle;
        private String moduleCode;
        private int jobWorkload;
        private Integer assignedWorkload;
        private int effectiveWorkload;

        public String getApplicationId() {
            return applicationId;
        }

        public void setApplicationId(String applicationId) {
            this.applicationId = applicationId;
        }

        public String getTaEmail() {
            return taEmail;
        }

        public void setTaEmail(String taEmail) {
            this.taEmail = taEmail;
        }

        public String getTaName() {
            return taName;
        }

        public void setTaName(String taName) {
            this.taName = taName;
        }

        public String getPositionTitle() {
            return positionTitle;
        }

        public void setPositionTitle(String positionTitle) {
            this.positionTitle = positionTitle;
        }

        public String getModuleCode() {
            return moduleCode;
        }

        public void setModuleCode(String moduleCode) {
            this.moduleCode = moduleCode;
        }

        public int getJobWorkload() {
            return jobWorkload;
        }

        public void setJobWorkload(int jobWorkload) {
            this.jobWorkload = jobWorkload;
        }

        public Integer getAssignedWorkload() {
            return assignedWorkload;
        }

        public void setAssignedWorkload(Integer assignedWorkload) {
            this.assignedWorkload = assignedWorkload;
        }

        public int getEffectiveWorkload() {
            return effectiveWorkload;
        }

        public void setEffectiveWorkload(int effectiveWorkload) {
            this.effectiveWorkload = effectiveWorkload;
        }
    }
}
