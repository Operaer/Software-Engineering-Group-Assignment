package com.bupt.ta.servlet;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.TAProfile;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.ApplicationStorage;
import com.bupt.ta.storage.ProfileStorage;

/**
 * Servlet handling MO-side application review and sorting requests.
 * <p>
 * Mapped URL: /secure/mo/application-management<br>
 * Provides MO users with full functionality to view and manage TA applications, including filtering
 * applications by position, sorting by GPA, skills, or submission time, and batch updating
 * application statuses. Applicant GPA and skill data are loaded from {@link TAProfile} records,
 * supporting sorting based on profile attributes.
 * </p>
 */
@WebServlet(name = "ApplicationManagementServlet", urlPatterns = "/secure/mo/application-management")
public class ApplicationManagementServlet extends BaseServlet {

    /**
     * Handles GET requests to display the application management page.
     * <p>
     * Requires the user to have the MO role permission. Loads all application records, optionally
     * filters by position ID, and loads each applicant's TAProfile data for sorting. Supported
     * sorting options include GPA, skill count, and submission time, in ascending or descending order.
     * </p>
     *
     * @param req  HTTP request, may contain jobId, sortBy, order parameters
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during forwarding or redirect
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePermission(req, resp, User.Role.MO)) {
            return;
        }

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        List<Application> applications = storage.findAll();
        String jobId = req.getParameter("jobId");
        if (jobId != null && !jobId.isBlank()) {
            applications = applications.stream()
                    .filter(application -> jobId.equals(application.getPositionId()))
                    .collect(java.util.stream.Collectors.toList());
            req.setAttribute("selectedJobId", jobId);
        }
        ProfileStorage profileStorage = new ProfileStorage(getServletContext());
        Map<String, TAProfile> profilesByEmail = loadProfiles(applications, profileStorage);
        String sortBy = normalizeSortBy(req.getParameter("sortBy"));
        String order = normalizeOrder(req.getParameter("order"), sortBy);
        applications.sort(buildComparator(sortBy, order, profilesByEmail));

        req.setAttribute("applications", applications);
        req.setAttribute("profilesByEmail", profilesByEmail);
        req.setAttribute("sortBy", sortBy);
        req.setAttribute("order", order);
        forwardTo(req, resp, "/secure/mo/application_list.jsp");
    }

    /**
     * Handles POST requests to batch update application statuses.
     * <p>
     * Receives one or more application IDs and a target status, applies the status change to all
     * selected applications, and records the current MO user as the operator. Returns an error
     * message if parameter validation fails or the status value is invalid.
     * </p>
     *
     * @param req  HTTP request containing applicationId (array) and status parameters
     * @param resp HTTP response
     * @throws ServletException if a Servlet exception occurs during forwarding
     * @throws IOException      if an IO error occurs during forwarding or redirect
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePermission(req, resp, User.Role.MO)) {
            return;
        }

        String[] applicationIds = req.getParameterValues("applicationId");
        String status = req.getParameter("status");

        if (applicationIds == null || applicationIds.length == 0 || status == null) {
            req.setAttribute("error", "Select at least one application and a target status.");
            doGet(req, resp);
            return;
        }

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        User currentUser = getCurrentUser(req);
        String operator = currentUser != null ? currentUser.getEmail() : "mo";

        try {
            Application.Status newStatus = Application.Status.valueOf(status);
            storage.updateStatus(java.util.Arrays.asList(applicationIds), newStatus, operator);
            req.setAttribute("success", applicationIds.length == 1 ?
                    "Application status updated." :
                    applicationIds.length + " applications updated.");
        } catch (Exception e) {
            req.setAttribute("error", "Error updating status: " + e.getMessage());
        }

        doGet(req, resp);
    }

    /**
     * Loads TAProfile data for all applicants in the application list.
     * <p>
     * Iterates through the application list, loads the corresponding profile data by TA email,
     * and stores it in a Map keyed by email for subsequent sorting.
     * </p>
     *
     * @param applications   The list of applications
     * @param profileStorage The profile data storage object
     * @return A mapping from email to TAProfile
     */
    private Map<String, TAProfile> loadProfiles(List<Application> applications, ProfileStorage profileStorage) {
        Map<String, TAProfile> profiles = new HashMap<>();
        for (Application application : applications) {
            String email = application.getTaEmail();
            if (email != null && !profiles.containsKey(email)) {
                profiles.put(email, profileStorage.load(email));
            }
        }
        return profiles;
    }

    /**
     * Normalizes the sort field: only allows gpa, skills, and appliedAt sorting options.
     * Defaults to appliedAt if the input is invalid.
     *
     * @param sortBy The original sort field parameter
     * @return The normalized sort field
     */
    private String normalizeSortBy(String sortBy) {
        if ("gpa".equals(sortBy) || "skills".equals(sortBy) || "appliedAt".equals(sortBy)) {
            return sortBy;
        }
        return "appliedAt";
    }

    /**
     * Normalizes the sort order: only allows asc and desc. Returns a default based on the sort field
     * if the input is invalid (appliedAt defaults to descending, others default to ascending).
     *
     * @param order  The original sort order parameter
     * @param sortBy The current sort field
     * @return The normalized sort order (asc or desc)
     */
    private String normalizeOrder(String order, String sortBy) {
        if ("asc".equalsIgnoreCase(order) || "desc".equalsIgnoreCase(order)) {
            return order.toLowerCase(Locale.ROOT);
        }
        return "appliedAt".equals(sortBy) ? "desc" : "asc";
    }

    /**
     * Builds an application comparator based on the sort field and order.
     * <p>
     * GPA sorting places null values at the end; skills sorting uses case-insensitive string
     * comparison; submission time sorting follows natural order. When values are equal, the
     * application ID is used as the secondary sort key.
     * </p>
     *
     * @param sortBy          The sort field (gpa, skills, or appliedAt)
     * @param order           The sort order (asc or desc)
     * @param profilesByEmail The mapping from email to TAProfile
     * @return The constructed comparator object
     */
    private Comparator<Application> buildComparator(String sortBy, String order, Map<String, TAProfile> profilesByEmail) {
        Comparator<Application> comparator;
        if ("gpa".equals(sortBy)) {
            Comparator<Double> gpaComparator = "desc".equals(order)
                    ? Comparator.nullsLast(Comparator.reverseOrder())
                    : Comparator.nullsLast(Comparator.naturalOrder());
            comparator = Comparator.comparing(
                    app -> getProfileGpa(profilesByEmail.get(app.getTaEmail())),
                    gpaComparator);
        } else if ("skills".equals(sortBy)) {
            comparator = Comparator.comparing(
                    app -> getProfileSkills(profilesByEmail.get(app.getTaEmail())),
                    String.CASE_INSENSITIVE_ORDER);
        } else {
            comparator = Comparator.comparing(Application::getAppliedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("desc".equals(order) && !"gpa".equals(sortBy)) {
            comparator = comparator.reversed();
        }
        return comparator.thenComparing(Application::getId, Comparator.nullsLast(String::compareTo));
    }

    /**
     * Retrieves the GPA value from a TAProfile, returning null if the profile does not exist.
     *
     * @param profile The TA profile object
     * @return The GPA value, or null if profile is null
     */
    private Double getProfileGpa(TAProfile profile) {
        return profile == null ? null : profile.getGpa();
    }

    /**
     * Retrieves the skills string from a TAProfile, joining multiple skills with a comma and space.
     *
     * @param profile The TA profile object
     * @return The skills string, or an empty string if profile or skills are null
     */
    private String getProfileSkills(TAProfile profile) {
        if (profile == null || profile.getSkills() == null) {
            return "";
        }
        return String.join(", ", profile.getSkills());
    }
}
