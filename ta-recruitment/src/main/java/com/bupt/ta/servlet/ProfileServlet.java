package com.bupt.ta.servlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.TAProfile;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.ProfileStorage;

/**
 * Servlet for TA profile management, including profile viewing, resume upload validation,
 * resume removal, and profile persistence.
 *
 * <p>Supports file upload constraints for resume format and size, and provides
 * real-time upload guidance on the profile page.</p>
 *
 * @author Operaer
 * @since 2026-05-19
 */
@WebServlet(name = "ProfileServlet", urlPatterns = "/secure/ta/profile")
@MultipartConfig(fileSizeThreshold = 1024 * 512, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class ProfileServlet extends BaseServlet {

    /**
     * Handle GET requests for the TA profile page.
     *
     * <p>Loads the current user's profile from storage and forwards to the JSP.</p>
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @throws ServletException on servlet failure
     * @throws IOException on input/output failure
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requireLogin(req, resp);

        User user = getCurrentUser(req);
        ProfileStorage storage = new ProfileStorage(getServletContext());
        TAProfile profile = storage.load(user.getEmail());
        if (profile == null) {
            profile = new TAProfile(user.getEmail());
        }

        String redirectAfterProfile = req.getParameter("redirectAfterProfile");
        if (redirectAfterProfile != null && !redirectAfterProfile.isBlank()) {
            req.setAttribute("redirectAfterProfile", redirectAfterProfile);
        }

        req.setAttribute("profile", profile);
        forwardTo(req, resp, "/secure/ta/profile.jsp");
    }

    private static final long MAX_RESUME_SIZE = 5 * 1024 * 1024;
    private static final String[] ALLOWED_RESUME_EXTENSIONS = {".pdf"};

    /**
     * Handle POST requests to save profile changes or manage resume upload/removal.
     *
     * <p>Validates resume file type and size, persists profile updates, and supports
     * resume removal action.</p>
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @throws ServletException on servlet failure
     * @throws IOException on input/output failure
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requireLogin(req, resp);

        User user = getCurrentUser(req);
        ProfileStorage storage = new ProfileStorage(getServletContext());
        TAProfile profile = storage.load(user.getEmail());
        if (profile == null) {
            profile = new TAProfile(user.getEmail());
        }

        String action = req.getParameter("action");
        if ("removeResume".equals(action)) {
            removeResume(profile);
            storage.save(profile);
            req.setAttribute("profile", profile);
            req.setAttribute("success", "Resume upload has been removed.");
            forwardTo(req, resp, "/secure/ta/profile.jsp");
            return;
        }

        String name = req.getParameter("name");
        String studentId = req.getParameter("studentId");
        String major = req.getParameter("major");
        String phone = req.getParameter("phone");
        String gpaRaw = req.getParameter("gpa");
        String skillsRaw = req.getParameter("skills");

        profile.setName(name);
        profile.setStudentId(studentId);
        profile.setMajor(major);
        profile.setPhone(phone);
        if (gpaRaw != null && !gpaRaw.isBlank()) {
            try {
                profile.setGpa(Double.parseDouble(gpaRaw.trim()));
            } catch (NumberFormatException e) {
                req.setAttribute("uploadError", "GPA must be a valid number.");
                req.setAttribute("profile", profile);
                forwardTo(req, resp, "/secure/ta/profile.jsp");
                return;
            }
        }
        if (skillsRaw != null) {
            String[] parts = skillsRaw.split(",");
            profile.getSkills().clear();
            for (String part : parts) {
                String t = part.trim();
                if (!t.isEmpty()) {
                    profile.getSkills().add(t);
                }
            }
        }

        Part resumePart = req.getPart("resume");
        if (resumePart != null && resumePart.getSize() > 0) {
            String submittedName = resumePart.getSubmittedFileName();
            String extension = "";
            if (submittedName != null) {
                int dot = submittedName.lastIndexOf('.');
                if (dot > 0) {
                    extension = submittedName.substring(dot).toLowerCase();
                }
            }

            if (!isAllowedResumeExtension(extension)) {
                req.setAttribute("uploadError", "Unsupported resume format. Only PDF documents are allowed.");
                req.setAttribute("profile", profile);
                forwardTo(req, resp, "/secure/ta/profile.jsp");
                return;
            }

            if (resumePart.getSize() > MAX_RESUME_SIZE) {
                req.setAttribute("uploadError", "Resume file is too large. Please upload a file ≤ 5MB.");
                req.setAttribute("profile", profile);
                forwardTo(req, resp, "/secure/ta/profile.jsp");
                return;
            }

            String fileName = user.getUsername().replaceAll("[^a-zA-Z0-9]", "_")
                    + "_" + Instant.now().toEpochMilli() + extension;

            File uploads = new File(getServletContext().getRealPath(AppConfig.UPLOAD_DIR));
            if (!uploads.exists()) {
                Files.createDirectories(uploads.toPath());
            }

            File dest = new File(uploads, fileName);
            try {
                Files.copy(resumePart.getInputStream(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                String oldResume = profile.getResumeFileName();
                profile.setResumeFileName(fileName);
                if (oldResume != null && !oldResume.isBlank() && !oldResume.equals(fileName)) {
                    deleteStoredResume(oldResume);
                }
                req.setAttribute("uploadMessage", "Resume uploaded successfully: " + fileName);
                req.setAttribute("uploadSuccess", true);
            } catch (IOException e) {
                req.setAttribute("uploadError", "Resume upload failed. Please try again.");
            }
        }

        storage.save(profile);

        String redirectAfterProfile = req.getParameter("redirectAfterProfile");
        if (redirectAfterProfile != null && !redirectAfterProfile.isBlank()) {
            resp.sendRedirect(req.getContextPath() + redirectAfterProfile);
            return;
        }

        req.setAttribute("profile", profile);
        req.setAttribute("success", "Profile saved successfully.");
        forwardTo(req, resp, "/secure/ta/profile.jsp");
    }

    /**
     * Returns true when the given file extension is an allowed resume type.
     *
     * @param extension the file extension to check, including dot
     * @return true if extension is permitted
     */
    private boolean isAllowedResumeExtension(String extension) {
        for (String allowed : ALLOWED_RESUME_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove the stored resume association and delete the physical file.
     *
     * @param profile the TA profile containing the resume reference
     */
    private void removeResume(TAProfile profile) {
        String fileName = profile.getResumeFileName();
        if (fileName != null && !fileName.isBlank()) {
            deleteStoredResume(fileName);
            profile.setResumeFileName(null);
        }
    }

    /**
     * Delete a resume file from server storage if it exists.
     *
     * @param fileName the stored resume file name
     */
    private void deleteStoredResume(String fileName) {
        File uploads = new File(getServletContext().getRealPath(AppConfig.UPLOAD_DIR));
        File oldFile = new File(uploads, fileName);
        if (oldFile.exists()) {
            oldFile.delete();
        }
    }
}
