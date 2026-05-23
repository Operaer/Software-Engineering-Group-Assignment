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

/**
 * Servlet handling TA-side application and position browsing requests.
 * <p>
 * Mapped URLs: /secure/ta/applications, /secure/ta/positions, /secure/ta/positions/*<br>
 * Provides TA users with functionalities for viewing their application list, browsing available
 * positions, filtering positions (by course/skills/GPA), viewing position details, and
 * submitting applications.
 * </p>
 */
@WebServlet(name = "ApplicationServlet", urlPatterns = {
        "/secure/ta/applications",
        "/secure/ta/positions",
        "/secure/ta/positions/*"
})
public class ApplicationServlet extends BaseServlet {

    /**
     * Handles GET requests to display the application list or available positions.
     * <p>
     * Functionality differs based on request path:<br>
     * - /secure/ta/positions/*: Browses the available position list or views position details<br>
     * - /secure/ta/applications: Views the current TA's application list, supports filtering
     *   available positions by course, skills, and GPA conditions, and handles success or
     *   already-applied notification messages.
     * </p>
     *
     * @param req  HTTP request, may contain filter parameters such as courseKeyword, skillKeyword, minimumGpa
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during forwarding or redirect
     */
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

    /**
     * Handles POST requests to submit a new position application.
     * <p>
     * Receives the jobId parameter, verifies that the position exists and is applicable,
     * checks for duplicate applications, and then creates a new application record.
     * </p>
     *
     * @param req  HTTP request containing the jobId parameter
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during forwarding or redirect
     */
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
     * Retrieves all currently applicable positions from JobStorage.
     *
     * @param jobStorage The job data storage object
     * @return A list of applicable positions
     */
    private List<Job> findApplicableJobs(JobStorage jobStorage) {
        return jobStorage.findAll().stream()
                .filter(this::isApplicable)
                .collect(Collectors.toList());
    }

    /**
     * Determines whether a position is applicable: status is open and deadline is not before today.
     *
     * @param job The job object to evaluate
     * @return true if applicable, false otherwise
     */
    private boolean isApplicable(Job job) {
        return job != null
                && Job.STATUS_OPEN.equals(job.getStatus())
                && job.getDeadline() != null
                && !job.getDeadline().isBefore(LocalDate.now());
    }

    /**
     * Determines whether the user has the TA role.
     *
     * @param user The user object to evaluate
     * @return true if the user is a TA, false otherwise
     */
    private boolean isTaUser(User user) {
        return user != null && user.getRole() == User.Role.TA;
    }

    /**
     * Filters the job list by course keyword, skill keyword, and minimum GPA.
     *
     * @param jobs          The list of jobs to filter
     * @param courseKeyword Course keyword (optional)
     * @param skillKeyword  Skill keyword (optional)
     * @param minimumGpa    Minimum GPA requirement (optional)
     * @return The filtered list of jobs
     */
    private List<Job> filterApplicableJobs(List<Job> jobs, String courseKeyword, String skillKeyword, Double minimumGpa) {
        return jobs.stream()
                .filter(job -> courseKeyword == null || containsIgnoreCase(job.getModuleCode(), courseKeyword))
                .filter(job -> skillKeyword == null || containsIgnoreCase(job.getRequirements(), skillKeyword))
                .filter(job -> minimumGpa == null || extractMinimumGpa(job.getRequirements()) <= minimumGpa)
                .collect(Collectors.toList());
    }

    /**
     * Normalizes a keyword: trims leading/trailing spaces, returns null if empty.
     *
     * @param value The original keyword
     * @return The normalized keyword, or null if empty
     */
    private String normalizeKeyword(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Parses a string into a Double, returning null if parsing fails.
     *
     * @param value The numeric string to parse
     * @return The parsed Double value, or null if parsing fails
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
     * Checks whether a string contains the specified keyword (case-insensitive).
     *
     * @param value   The string to search
     * @param keyword The keyword to find
     * @return true if the keyword is found, false otherwise
     */
    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    /**
     * Extracts the minimum GPA requirement from a position's requirement description.
     * <p>
     * Uses a regular expression to match patterns such as "GPA >= 3.0", "GPA min 3.5", etc.
     * Returns 0.0 if no match is found.
     * </p>
     *
     * @param requirements The position requirement description text
     * @return The parsed minimum GPA value, or 0.0 if not found
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
     * Determines whether the current request is a position browsing request.
     *
     * @param req HTTP request
     * @return true if the request path starts with /secure/ta/positions
     */
    private boolean isPositionsRequest(HttpServletRequest req) {
        String servletPath = req.getServletPath();
        return servletPath != null && servletPath.startsWith("/secure/ta/positions");
    }

    /**
     * Extracts the position ID from the request path.
     *
     * @param req HTTP request whose path info contains the position ID
     * @return The position ID string, or null if no valid path info is present
     */
    private String extractPositionId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
            return null;
        }
        return pathInfo.substring(1);
    }

    /**
     * Logs TA-side position filter diagnostic information to the console for debugging and traceability.
     *
     * @param req           The current HTTP request
     * @param courseKeyword The submitted course keyword
     * @param skillKeyword  The submitted skill keyword
     * @param minimumGpa    The submitted minimum GPA value
     * @param availableJobs The position list before advanced filtering
     * @param filteredJobs  The position list after advanced filtering
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
