package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.User;
import com.bupt.ta.storage.ApplicationStorage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "ApplicationServlet", urlPatterns = "/secure/ta/applications")
public class ApplicationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        List<Application> applications = storage.findByTaEmail(user.getEmail());

        req.setAttribute("applications", applications);
        req.getRequestDispatcher("/secure/ta/applications.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        String positionId = req.getParameter("positionId");
        String positionTitle = req.getParameter("positionTitle");
        if (positionId == null || positionId.isBlank() || positionTitle == null || positionTitle.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/secure/ta/applications");
            return;
        }

        ApplicationStorage storage = new ApplicationStorage(getServletContext());
        Application application = storage.createNew(user.getEmail(), positionId, positionTitle);

        req.setAttribute("success", "申请已提交：" + application.getPositionTitle());
        doGet(req, resp);
    }
}
