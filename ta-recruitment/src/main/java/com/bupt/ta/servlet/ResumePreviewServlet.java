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

/**
 * Streams resume files for ADMIN/MO users to preview (display inline, not download).
 *
 * <p>Only users with ADMIN or MO roles can access this endpoint. This servlet
 * validates both user role and file ownership via sanitized TA email/username prefixes
 * in the filename to prevent unauthorized file access.</p>
 */
@WebServlet(name = "ResumePreviewServlet", urlPatterns = "/secure/resume-preview")
public class ResumePreviewServlet extends BaseServlet {

    /**
     * Handles GET requests to preview a TA's resume file for administrative review.
     *
     * <p>Request parameters:</p>
     * <ul>
     *   <li>file: the sanitized filename of the resume to stream</li>
     *   <li>ta: the email address of the TA whose resume is being viewed</li>
     * </ul>
     *
     * <p>Validates that:</p>
     * <ul>
     *   <li>user is logged in (requireLogin enforced)</li>
     *   <li>user has ADMIN or MO role</li>
     *   <li>filename ends with .pdf and starts with sanitized TA email/username prefix</li>
     *   <li>file exists in the configured upload directory</li>
     * </ul>
     *
     * <p>Returns 400 if parameters are missing, 403 if unauthorized or filename invalid,
     * 404 if file not found. PDF is served inline (displayed in browser, not downloaded).</p>
     *
     * @param req the HTTP request with 'file' and 'ta' parameters
     * @param resp the HTTP response for streaming the PDF inline
     * @throws ServletException on servlet failure
     * @throws IOException on input/output failure during file streaming
     */
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

        // Verify file name is safe (should start with sanitized TA username or email prefix).
        String sanitizedEmail = taEmail.replaceAll("[^a-zA-Z0-9]", "_");
        String sanitizedUserId = taEmail.contains("@") ? taEmail.substring(0, taEmail.indexOf('@')).replaceAll("[^a-zA-Z0-9]", "_") : sanitizedEmail;
        if (!fileName.toLowerCase().endsWith(".pdf") ||
                !(fileName.startsWith(sanitizedUserId) || fileName.startsWith(sanitizedEmail))) {
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
