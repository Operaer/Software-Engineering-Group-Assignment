package com.bupt.ta.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.UserStorage;

/**
 * Resume Preview Servlet (for ADMIN/MO use).
 *
 * <p>Only allows users with ADMIN and MO roles to preview TA resume files. It verifies
 * file ownership by checking that the file name starts with the sanitized TA username
 * or email prefix via the TA email parameter, preventing unauthorized access. The PDF
 * content is displayed inline in the browser.</p>
 */
@WebServlet(name = "ResumePreviewServlet", urlPatterns = "/secure/resume-preview")
public class ResumePreviewServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requireLogin(req, resp);

        User user = getCurrentUser(req);
        String fileName = req.getParameter("file");
        String taEmail = req.getParameter("ta");

        if (fileName == null || fileName.isBlank() || taEmail == null || taEmail.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Only ADMIN and MO can preview resumes
        if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.MO) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Verify file ownership: the filename uses the TA's username (from ProfileServlet),
        // which may differ from the email prefix. Check both email-derived and username-derived prefixes.
        boolean fileOwnershipVerified = false;
        if (fileName.toLowerCase().endsWith(".pdf")) {
            String sanitizedEmail = taEmail.replaceAll("[^a-zA-Z0-9]", "_");
            String sanitizedUserId = taEmail.contains("@")
                    ? taEmail.substring(0, taEmail.indexOf('@')).replaceAll("[^a-zA-Z0-9]", "_")
                    : sanitizedEmail;
            if (fileName.startsWith(sanitizedUserId) || fileName.startsWith(sanitizedEmail)) {
                fileOwnershipVerified = true;
            }
            if (!fileOwnershipVerified) {
                try {
                    UserStorage userStorage = new UserStorage(getServletContext());
                    User taUser = userStorage.findByEmail(taEmail);
                    if (taUser != null) {
                        String sanitizedUsername = taUser.getUsername().replaceAll("[^a-zA-Z0-9]", "_");
                        if (fileName.startsWith(sanitizedUsername)) {
                            fileOwnershipVerified = true;
                        }
                    }
                } catch (Exception ignored) {
                    // UserStorage lookup failure: fall through to 403 below
                }
            }
        }
        if (!fileOwnershipVerified) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File file = new File(getServletContext().getRealPath(AppConfig.UPLOAD_DIR + "/" + fileName));
        if (!file.exists() || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Use inline to display in browser, not attachment for download
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        resp.setContentLengthLong(file.length());

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
}
