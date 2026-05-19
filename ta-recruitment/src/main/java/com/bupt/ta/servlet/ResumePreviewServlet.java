package com.bupt.ta.servlet;

import com.bupt.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Streams resume files for ADMIN/MO users to preview (display inline, not download).
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

        // Verify file name is safe (should start with sanitized email)
        String sanitizedEmail = taEmail.replaceAll("[^a-zA-Z0-9]", "_");
        if (!fileName.startsWith(sanitizedEmail)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File file = new File(getServletContext().getRealPath("/WEB-INF/uploads/" + fileName));
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
