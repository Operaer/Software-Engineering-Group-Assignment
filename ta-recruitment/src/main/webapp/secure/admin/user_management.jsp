<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>User Management (Demo)</h2>
            <p class="text-muted">View and manage user accounts and roles in the system.</p>
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
                    <th>Username</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th class="text-end">Action</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>ta1</td>
                    <td>ta1@example.com</td>
                    <td>TA</td>
                    <td><span class="badge status-accepted">Active</span></td>
                    <td class="text-end"><button class="btn btn-sm btn-secondary">Edit</button></td>
                </tr>
                <tr>
                    <td>mo1</td>
                    <td>mo1@example.com</td>
                    <td>MO</td>
                    <td><span class="badge status-pending">Pending</span></td>
                    <td class="text-end"><button class="btn btn-sm btn-secondary">Review</button></td>
                </tr>
                <tr>
                    <td>admin</td>
                    <td>admin@example.com</td>
                    <td>ADMIN</td>
                    <td><span class="badge status-accepted">Active</span></td>
                    <td class="text-end"><button class="btn btn-sm btn-secondary">View</button></td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
