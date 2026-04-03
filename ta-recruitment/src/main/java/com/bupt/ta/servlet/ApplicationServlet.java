package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.ApplicationStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "ApplicationServlet", urlPatterns = "/secure/ta/applications")
public class ApplicationServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requireLogin(req, resp);

        User user = getCurrentUser(req);
        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        List<Application> applications = storage.findByTaEmail(user.getEmail());

        req.setAttribute("applications", applications);
        forwardTo(req, resp, "/secure/ta/applications.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requireLogin(req, resp);

        User user = getCurrentUser(req);
        String positionId = req.getParameter("positionId");
        String positionTitle = req.getParameter("positionTitle");

        if (positionId == null || positionId.isBlank() || positionTitle == null || positionTitle.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/secure/ta/applications");
            return;
        }

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        Application application = storage.createNew(user.getEmail(), positionId, positionTitle);

        req.setAttribute("success", "Application submitted: " + application.getPositionTitle());
        doGet(req, resp);
    }
}
