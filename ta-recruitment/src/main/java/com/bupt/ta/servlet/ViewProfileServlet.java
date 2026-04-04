package com.bupt.ta.servlet;

import com.bupt.ta.model.TAProfile;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.ProfileStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ViewProfileServlet", urlPatterns = "/secure/mo/view-profile")
public class ViewProfileServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requireLogin(req, resp);
        requirePermission(req, resp, User.Role.MO); // Only MO and above can view

        String email = req.getParameter("email");
        if (email == null || email.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email parameter is required");
            return;
        }

        ProfileStorage storage = new ProfileStorage(getServletContext());
        TAProfile profile = storage.load(email.trim());
        if (profile == null) {
            req.setAttribute("error", "Profile not found for: " + email);
        } else {
            req.setAttribute("profile", profile);
            req.setAttribute("readOnly", true); // For display only
        }

        forwardTo(req, resp, "/secure/ta/profile.jsp");
    }
}