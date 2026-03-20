package com.bupt.ta.servlet;

import com.bupt.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        switch (user.getRole()) {
            case TA:
                req.getRequestDispatcher("/secure/ta/dashboard.jsp").forward(req, resp);
                break;
            case MO:
                req.getRequestDispatcher("/secure/mo/dashboard.jsp").forward(req, resp);
                break;
            case ADMIN:
                req.getRequestDispatcher("/secure/admin/dashboard.jsp").forward(req, resp);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/");
                break;
        }
    }
}
