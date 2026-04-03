package com.bupt.ta.servlet;

import com.bupt.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Streams uploaded resume files for authenticated TA users.
 */
@WebServlet(name = "ResumeDownloadServlet", urlPatterns = "/secure/ta/resume")
public class ResumeDownloadServlet extends BaseServlet {

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
        // (filename includes sanitized email prefix)
        if (!fileName.startsWith(user.getEmail().replaceAll("[^a-zA-Z0-9]", "_"))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File file = new File(getServletContext().getRealPath("/WEB-INF/uploads/" + fileName));
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
