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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@WebServlet(name = "ApplicationServlet", urlPatterns = {
        "/secure/ta/applications",
        "/secure/ta/positions",
        "/secure/ta/positions/*"
})
/**
 * Handles TA-facing application actions and available-position browsing.
 *
 * <p>The servlet reuses the same applicable-position filtering pipeline for
 * both the application page and the dedicated available-positions page. It also
 * serves position-detail requests under {@code /secure/ta/positions/*}.</p>
 */
public class ApplicationServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }

        User user = getCurrentUser(req);
        ApplicationStorage appStorage = new ApplicationStorage(getServletContext());
        JobStorage jobStorage = new JobStorage(getServletContext());

        List<Application> applications = appStorage.findByTaEmail(user.getEmail());
        List<Job> availableJobs = findApplicableJobs(jobStorage);
        String courseKeyword = normalizeKeyword(req.getParameter("courseKeyword"));
        String skillKeyword = normalizeKeyword(req.getParameter("skillKeyword"));
        Double minimumGpa = parseOptionalDouble(req.getParameter("minimumGpa"));
        List<Job> filteredJobs = filterApplicableJobs(availableJobs, courseKeyword, skillKeyword, minimumGpa);
        logAvailablePositionFilter(req, courseKeyword, skillKeyword, minimumGpa, availableJobs, filteredJobs);

        if (isPositionsRequest(req)) {
            if (!isTaUser(user)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only TA users can access available positions");
                return;
            }
            String positionId = extractPositionId(req);
            if (positionId != null) {
                Job job = jobStorage.findById(positionId);
                if (!isApplicable(job)) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Position not found");
                    return;
                }
                req.setAttribute("job", job);
                forwardTo(req, resp, "/secure/ta/position_details.jsp");
                return;
            }

            req.setAttribute("availableJobs", filteredJobs);
            req.setAttribute("courseKeyword", courseKeyword);
            req.setAttribute("skillKeyword", skillKeyword);
            req.setAttribute("minimumGpa", minimumGpa);
            forwardTo(req, resp, "/secure/ta/available_positions.jsp");
            return;
        }

        // Handle success message from URL parameter
        String successParam = req.getParameter("success");
        String positionParam = req.getParameter("position");
        if ("true".equals(successParam)) {
            String positionName = positionParam != null ? positionParam : "this position";
            req.setAttribute("success", "Application submitted successfully: " + positionName);
            req.setAttribute("showSuccessModal", true);
        }
        
        // Handle already applied message from URL parameter
        String alreadyAppliedParam = req.getParameter("alreadyApplied");
        if ("true".equals(alreadyAppliedParam)) {
            req.setAttribute("alreadyApplied", "You have already submitted an application for this position. You can view your application status in the 'My Applications' section.");
        }
        
        req.setAttribute("applications", applications);
        req.setAttribute("availableJobs", filteredJobs);
        req.setAttribute("courseKeyword", courseKeyword);
        req.setAttribute("skillKeyword", skillKeyword);
        req.setAttribute("minimumGpa", minimumGpa);
        forwardTo(req, resp, "/secure/ta/applications.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) {
            return;
        }

        User user = getCurrentUser(req);
        String jobId = req.getParameter("jobId");

        if (jobId == null || jobId.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/secure/ta/applications");
            return;
        }

        JobStorage jobStorage = new JobStorage(getServletContext());
        Job job = jobStorage.findById(jobId);
        if (!isApplicable(job)) {
            req.setAttribute("error", "This position is no longer available.");
            doGet(req, resp);
            return;
        }

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        
        // Check if user has already applied for this position
        if (storage.hasApplied(user.getEmail(), jobId)) {
            req.setAttribute("alreadyApplied", "You have already submitted an application for this position. You can view your application status in the 'My Applications' section.");
            doGet(req, resp);
            return;
        }
        
        Application application = storage.createNew(user.getEmail(), jobId, job.getTitle());

        req.setAttribute("success", "Application submitted successfully: " + application.getPositionTitle());
        req.setAttribute("showSuccessModal", true);
        doGet(req, resp);
    }

    /**
     * Loads all positions that are currently visible to TA users.
     *
     * @param jobStorage storage used to retrieve persisted positions
     * @return positions that are open and not past their deadline
     */
    private List<Job> findApplicableJobs(JobStorage jobStorage) {
        return jobStorage.findAll().stream()
                .filter(this::isApplicable)
                .collect(Collectors.toList());
    }

    /**
     * Determines whether a position may still be shown and applied for by a TA.
     *
     * @param job position to inspect
     * @return {@code true} when the position is open and its deadline has not passed
     */
    private boolean isApplicable(Job job) {
        return job != null
                && Job.STATUS_OPEN.equals(job.getStatus())
                && job.getDeadline() != null
                && !job.getDeadline().isBefore(LocalDate.now());
    }

    /**
     * Checks whether the current authenticated user is exactly a TA user.
     *
     * @param user authenticated user
     * @return {@code true} only for TA-role accounts
     */
    private boolean isTaUser(User user) {
        return user != null && user.getRole() == User.Role.TA;
    }

    /**
     * Applies the TA-side advanced filters using AND semantics.
     *
     * <p>The course keyword is matched against the module code, the skill
     * keyword is matched against the requirements text, and the supplied GPA is
     * compared with the GPA requirement parsed from the requirements text.</p>
     *
     * @param jobs candidate applicable positions
     * @param courseKeyword optional module-code keyword
     * @param skillKeyword optional requirements keyword
     * @param minimumGpa optional GPA available to the TA
     * @return positions matching every provided filter
     */
    private List<Job> filterApplicableJobs(List<Job> jobs, String courseKeyword, String skillKeyword, Double minimumGpa) {
        return jobs.stream()
                .filter(job -> courseKeyword == null || containsIgnoreCase(job.getModuleCode(), courseKeyword))
                .filter(job -> skillKeyword == null || containsIgnoreCase(job.getRequirements(), skillKeyword))
                .filter(job -> minimumGpa == null || extractMinimumGpa(job.getRequirements()) <= minimumGpa)
                .collect(Collectors.toList());
    }

    /**
     * Normalizes an optional keyword submitted from a filter form.
     *
     * @param value raw form value
     * @return trimmed value, or {@code null} when blank
     */
    private String normalizeKeyword(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Parses an optional decimal form value without failing the request.
     *
     * @param value raw form value
     * @return parsed decimal value, or {@code null} when absent or invalid
     */
    private Double parseOptionalDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Performs a null-safe, case-insensitive substring check.
     *
     * @param value source text
     * @param keyword keyword to search for
     * @return {@code true} when the source contains the keyword
     */
    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    /**
     * Extracts a numeric GPA requirement from free-form requirement text.
     *
     * <p>Supported examples include {@code "GPA 3.0"},
     * {@code "GPA >= 3.0"}, and {@code "minimum GPA 3.0"}. Positions without a
     * parsable GPA requirement are treated as requiring {@code 0.0}.</p>
     *
     * @param requirements free-form position requirements
     * @return parsed GPA requirement, or {@code 0.0} when none is present
     */
    private double extractMinimumGpa(String requirements) {
        if (requirements == null || requirements.isBlank()) {
            return 0.0;
        }
        Matcher matcher = Pattern.compile("(?i)gpa\\s*(?:>=|>|at least|minimum|min)?\\s*([0-4](?:\\.\\d+)?)").matcher(requirements);
        if (!matcher.find()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Detects whether the current request targets the available-position flow.
     *
     * @param req current HTTP request
     * @return {@code true} for {@code /secure/ta/positions} routes
     */
    private boolean isPositionsRequest(HttpServletRequest req) {
        String servletPath = req.getServletPath();
        return servletPath != null && servletPath.startsWith("/secure/ta/positions");
    }

    /**
     * Extracts a position identifier from the path-info suffix of a detail URL.
     *
     * @param req current HTTP request
     * @return position id, or {@code null} for the list route
     */
    private String extractPositionId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
            return null;
        }
        return pathInfo.substring(1);
    }

    /**
     * Writes diagnostic output for TA-side position filtering.
     *
     * @param req current HTTP request
     * @param courseKeyword submitted course keyword
     * @param skillKeyword submitted skill keyword
     * @param minimumGpa submitted GPA value
     * @param availableJobs positions before advanced filtering
     * @param filteredJobs positions after advanced filtering
     */
    private void logAvailablePositionFilter(HttpServletRequest req, String courseKeyword, String skillKeyword,
                                            Double minimumGpa, List<Job> availableJobs, List<Job> filteredJobs) {
        System.out.println("[TA Position Filter] requestURI=" + req.getRequestURI());
        System.out.println("[TA Position Filter] courseKeyword=" + courseKeyword
                + ", skillKeyword=" + skillKeyword
                + ", minimumGpa=" + minimumGpa);
        System.out.println("[TA Position Filter] beforeCount=" + availableJobs.size()
                + ", afterCount=" + filteredJobs.size());
        for (Job job : availableJobs) {
            System.out.println("[TA Position Filter] beforeJob title=" + job.getTitle()
                    + ", moduleCode=" + job.getModuleCode()
                    + ", requirements=" + job.getRequirements()
                    + ", parsedGpaRequirement=" + extractMinimumGpa(job.getRequirements()));
        }
        for (Job job : filteredJobs) {
            System.out.println("[TA Position Filter] afterJob title=" + job.getTitle()
                    + ", moduleCode=" + job.getModuleCode()
                    + ", requirements=" + job.getRequirements()
                    + ", parsedGpaRequirement=" + extractMinimumGpa(job.getRequirements()));
        }
    }
}
