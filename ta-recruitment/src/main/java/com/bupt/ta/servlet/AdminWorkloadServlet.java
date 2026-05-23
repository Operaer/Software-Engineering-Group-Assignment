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
 * Admin Workload Management Servlet.
 *
 * <p>Displays TA workload summary information, identifies overloaded TAs, shows module
 * workload distribution, supports manual adjustment of assigned workload for accepted
 * TA applications, and provides filtering by TA name/email, module, and workload range.</p>
 *
 * @author Operaer
 * @since 2026-05-17
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

    /**
     * Builds a user display name index, mapping emails to usernames.
     *
     * @return the email-to-username mapping table
     * @throws IOException if reading user storage fails
     */
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

    /**
     * Checks whether a TA workload metric passes the filter criteria (name/email keyword, workload range).
     *
     * @param metric      the TA workload metric
     * @param taFilter    the TA name or email keyword
     * @param minWorkload the minimum workload
     * @param maxWorkload the maximum workload
     * @return true if it passes all filter criteria
     */
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
     * Converts a TA accumulator to a TA workload metric object.
     *
     * @param accumulator the TA accumulator
     * @return the TA workload metric
     */
    private AdminDashboardStats.TAWorkloadMetric toTAWorkloadMetric(TAAccumulator accumulator) {
        AdminDashboardStats.TAWorkloadMetric metric = new AdminDashboardStats.TAWorkloadMetric();
        metric.setTaEmail(accumulator.taEmail);
        metric.setTaName(accumulator.taName);
        metric.setAcceptedPositions(accumulator.acceptedPositions);
        metric.setTotalWorkload(accumulator.totalWorkload);
        metric.setModules(String.join(", ", new TreeSet<>(accumulator.modules)));
        return metric;
    }

    /**
     * Converts a module accumulator to a module workload metric object.
     *
     * @param accumulator the module accumulator
     * @return the module workload metric
     */
    private AdminDashboardStats.ModuleWorkloadMetric toModuleWorkloadMetric(ModuleAccumulator accumulator) {
        AdminDashboardStats.ModuleWorkloadMetric metric = new AdminDashboardStats.ModuleWorkloadMetric();
        metric.setModuleCode(accumulator.moduleCode);
        metric.setHiredTAs(accumulator.hiredTAs.size());
        metric.setAcceptedPositions(accumulator.acceptedPositions);
        metric.setTotalWorkload(accumulator.totalWorkload);
        return metric;
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
     * Parses an optional integer parameter, returns null for invalid formats.
     *
     * @param value the integer string
     * @return the parsed Integer, or null if parsing fails
     */
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

    /**
     * Parses an optional integer parameter, returns the default value for invalid formats.
     *
     * @param value    the integer string
     * @param fallback the default value
     * @return the parsed integer, or the default value if parsing fails
     */
    private int parseOptionalInteger(String value, int fallback) {
        Integer parsed = parseOptionalInteger(value);
        return parsed != null ? parsed : fallback;
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
     * Compares two strings for equality, ignoring case.
     *
     * @param expected the expected value
     * @param actual   the actual value
     * @return true if they are equal ignoring case
     */
    private boolean equalsIgnoreCase(String expected, String actual) {
        return expected != null && actual != null && expected.equalsIgnoreCase(actual);
    }

    /**
     * TA workload accumulator, used to count the number of accepted positions, total workload,
     * and associated module set for each TA.
     */
    private static class TAAccumulator {
        private final String taEmail;
        private final String taName;
        private int acceptedPositions;
        private int totalWorkload;
        private final Set<String> modules = new TreeSet<>();

        /**
         * Constructs a TA accumulator.
         *
         * @param taEmail the TA email
         * @param taName  the TA name
         */
        private TAAccumulator(String taEmail, String taName) {
            this.taEmail = taEmail;
            this.taName = taName;
        }
    }

    /**
     * Module workload accumulator, used to count the number of accepted positions, total workload,
     * and hired TA set for each module.
     */
    private static class ModuleAccumulator {
        private final String moduleCode;
        private int acceptedPositions;
        private int totalWorkload;
        private final Set<String> hiredTAs = new HashSet<>();

        /**
         * Constructs a module accumulator.
         *
         * @param moduleCode the module code
         */
        private ModuleAccumulator(String moduleCode) {
            this.moduleCode = moduleCode;
        }
    }

    /**
     * Workload assignment entry, recording the assignment details of a single TA for a position.
     */
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
