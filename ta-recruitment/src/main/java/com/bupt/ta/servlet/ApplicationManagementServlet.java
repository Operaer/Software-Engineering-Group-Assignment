package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.TAProfile;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.ApplicationStorage;
import com.bupt.ta.storage.ProfileStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "ApplicationManagementServlet", urlPatterns = "/secure/mo/application-management")
/**
 * Handles MO-side application review and applicant sorting.
 *
 * <p>Applicant GPA and skill values are loaded from {@link TAProfile} records
 * so the application list can be sorted by profile-backed attributes as well
 * as by application submission time.</p>
 */
public class ApplicationManagementServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePermission(req, resp, User.Role.MO)) {
            return;
        }

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        List<Application> applications = storage.findAll();
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePermission(req, resp, User.Role.MO)) {
            return;
        }

        String applicationId = req.getParameter("applicationId");
        String status = req.getParameter("status");

        if (applicationId == null || status == null) {
            resp.sendRedirect(req.getContextPath() + "/secure/mo/application-management");
            return;
        }

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        try {
            Application.Status newStatus = Application.Status.valueOf(status);
            storage.updateStatus(applicationId, newStatus);
            req.setAttribute("success", "Application status updated");
        } catch (Exception e) {
            req.setAttribute("error", "Error updating status: " + e.getMessage());
        }

        doGet(req, resp);
    }

    /**
     * Loads TA profiles required for rendering and sorting the application list.
     *
     * @param applications applications currently shown to the MO
     * @param profileStorage storage used to load TA profiles
     * @return profile map keyed by TA email
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
     * Normalizes the supported sort column submitted by the UI.
     *
     * @param sortBy raw sort column
     * @return supported sort column, defaulting to {@code appliedAt}
     */
    private String normalizeSortBy(String sortBy) {
        if ("gpa".equals(sortBy) || "skills".equals(sortBy) || "appliedAt".equals(sortBy)) {
            return sortBy;
        }
        return "appliedAt";
    }

    /**
     * Normalizes the requested sort direction.
     *
     * @param order raw sort direction
     * @param sortBy normalized sort column
     * @return {@code asc} or {@code desc}
     */
    private String normalizeOrder(String order, String sortBy) {
        if ("asc".equalsIgnoreCase(order) || "desc".equalsIgnoreCase(order)) {
            return order.toLowerCase(Locale.ROOT);
        }
        return "appliedAt".equals(sortBy) ? "desc" : "asc";
    }

    /**
     * Builds the comparator used for MO-side applicant sorting.
     *
     * @param sortBy normalized sort column
     * @param order normalized sort direction
     * @param profilesByEmail TA profile lookup map
     * @return comparator for application rows
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
     * Reads the GPA value used for applicant sorting.
     *
     * @param profile TA profile, if available
     * @return GPA value, or {@code null} when no profile GPA exists
     */
    private Double getProfileGpa(TAProfile profile) {
        return profile == null ? null : profile.getGpa();
    }

    /**
     * Builds a comparable skill string for applicant sorting.
     *
     * @param profile TA profile, if available
     * @return comma-separated skills, or an empty string when absent
     */
    private String getProfileSkills(TAProfile profile) {
        if (profile == null || profile.getSkills() == null) {
            return "";
        }
        return String.join(", ", profile.getSkills());
    }
}
