package com.bupt.ta.servlet;

import com.bupt.ta.model.TAProfile;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.ProfileStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

@WebServlet(name = "ProfileServlet", urlPatterns = "/secure/ta/profile")
@MultipartConfig(fileSizeThreshold = 1024 * 512, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class ProfileServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requireLogin(req, resp);

        User user = getCurrentUser(req);
        ProfileStorage storage = new ProfileStorage(getServletContext());
        TAProfile profile = storage.load(user.getEmail());
        if (profile == null) {
            profile = new TAProfile(user.getEmail());
        }

        req.setAttribute("profile", profile);
        forwardTo(req, resp, "/secure/ta/profile.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requireLogin(req, resp);

        User user = getCurrentUser(req);
        String name = req.getParameter("name");
        String major = req.getParameter("major");
        String phone = req.getParameter("phone");
        String skillsRaw = req.getParameter("skills");

        TAProfile profile = new TAProfile(user.getEmail());
        profile.setName(name);
        profile.setMajor(major);
        profile.setPhone(phone);
        if (skillsRaw != null) {
            String[] parts = skillsRaw.split(",");
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
            int dot = submittedName.lastIndexOf('.');
            if (dot > 0) {
                extension = submittedName.substring(dot);
            }

            String fileName = user.getEmail().replaceAll("[^a-zA-Z0-9]", "_")
                    + "_" + Instant.now().toEpochMilli() + extension;

            File uploads = new File(getServletContext().getRealPath("/WEB-INF/uploads"));
            if (!uploads.exists()) {
                Files.createDirectories(uploads.toPath());
            }

            File dest = new File(uploads, fileName);
            try {
                Files.copy(resumePart.getInputStream(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                profile.setResumeFileName(fileName);
                req.setAttribute("uploadMessage", "Resume uploaded successfully: " + fileName);
            } catch (IOException e) {
                req.setAttribute("uploadError", "Resume upload failed. Please try again.");
            }
        }

        ProfileStorage storage = new ProfileStorage(getServletContext());
        storage.save(profile);

        req.setAttribute("profile", profile);
        req.setAttribute("success", "Profile saved successfully.");
        forwardTo(req, resp, "/secure/ta/profile.jsp");
    }
    }
}
