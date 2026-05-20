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
 * Streams uploaded resume files for authenticated TA users.
 * 
 * <p>Only allows each TA to download their own resume file. File access is validated
 * against the user's sanitized email/username prefix embedded in the filename.
 * PDF files are served inline using proper Content-Disposition headers.</p>
 */
@WebServlet(name = "ResumeDownloadServlet", urlPatterns = "/secure/ta/resume")
public class ResumeDownloadServlet extends BaseServlet {

    /**
     * Handles GET requests to download a TA's own resume file.
     * 
     * <p>Request parameters:</p>
     * <ul>
     *   <li>file: the sanitized filename of the resume to stream</li>
     * </ul>
     *
     * <p>Validates that:</p>
     * <ul>
     *   <li>user is logged in (requireLogin enforced)</li>
     *   <li>filename starts with user's email/username prefix for access control</li>
     *   <li>file exists in the configured upload directory</li>
     * </ul>
     *
     * <p>Returns 400 if filename is missing, 403 if unauthorized, 404 if file not found.</p>
     *
     * @param req the HTTP request with 'file' parameter
     * @param resp the HTTP response for streaming the PDF
     * @throws ServletException on servlet failure
     * @throws IOException on input/output failure during file streaming
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requireLogin(req, resp);

        User user = getCurrentUser(req);
        String fileName = req.getParameter("file");
        if (fileName == null || fileName.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Only allow TA to download their own resume
        // (filename includes a sanitized user ID prefix)
        String sanitizedUsername = user.getUsername().replaceAll("[^a-zA-Z0-9]", "_");
        String sanitizedEmail = user.getEmail().replaceAll("[^a-zA-Z0-9]", "_");
        if (!fileName.startsWith(sanitizedUsername) && !fileName.startsWith(sanitizedEmail)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File file = new File(getServletContext().getRealPath(AppConfig.UPLOAD_DIR + "/" + fileName));
        if (!file.exists() || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

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
