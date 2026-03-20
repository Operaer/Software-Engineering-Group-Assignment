<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Audit Logs (Demo)</h2>
            <p class="text-muted">Displays a simple log of key system events.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/admin/dashboard.jsp">Back to Admin Dashboard</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <table class="table table-hover">
                <thead>
                <tr>
                    <th>Time</th>
                    <th>User</th>
                    <th>Type</th>
                    <th>Description</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>2026-03-17 09:12</td>
                    <td>admin@example.com</td>
                    <td>User Management</td>
                    <td>Created MO account mo2</td>
                </tr>
                <tr>
                    <td>2026-03-16 18:25</td>
                    <td>admin@example.com</td>
                    <td>System Settings</td>
                    <td>Changed email notification toggle</td>
                </tr>
                <tr>
                    <td>2026-03-15 14:07</td>
                    <td>admin@example.com</td>
                    <td>Audit Logs</td>
                    <td>Viewed TA application list</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
