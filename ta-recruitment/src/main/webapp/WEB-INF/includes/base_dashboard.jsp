<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    String roleName = currentUser.getRole().name().toLowerCase();
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2><%= roleName.toUpperCase() %> Dashboard</h2>
            <p class="text-muted">Welcome, <strong><%= currentUser.getEmail() %></strong>!</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
    </div>

    <div class="row mt-4">
        <!-- Common cards for all roles -->
        <div class="col-md-3">
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">My Profile</h5>
                    <p class="card-text">Manage your personal information.</p>
                    <a href="<%= request.getContextPath() %>/secure/ta/profile" class="btn btn-sm btn-primary">Edit Profile</a>
                </div>
            </div>
        </div>

        <!-- Role-specific content -->
        <% if ("ta".equals(roleName)) { %>
            <div class="col-md-3">
                <div class="card mb-3">
                    <div class="card-body">
                        <h5 class="card-title">My Applications</h5>
                        <p class="card-text">View your submitted applications.</p>
                        <a href="<%= request.getContextPath() %>/secure/ta/applications" class="btn btn-sm btn-primary">View Applications</a>
                    </div>
                </div>
            </div>
        <% } %>

        <% if ("mo".equals(roleName) || "admin".equals(roleName)) { %>
            <div class="col-md-3">
                <div class="card mb-3">
                    <div class="card-body">
                        <h5 class="card-title">Application Management</h5>
                        <p class="card-text">Review and manage applications.</p>
                        <a href="<%= request.getContextPath() %>/secure/mo/application-management" class="btn btn-sm btn-primary">Manage Applications</a>
                    </div>
                </div>
            </div>
        <% } %>

        <% if ("admin".equals(roleName)) { %>
            <div class="col-md-3">
                <div class="card mb-3">
                    <div class="card-body">
                        <h5 class="card-title">User Management</h5>
                        <p class="card-text">Manage system users.</p>
                        <a href="<%= request.getContextPath() %>/secure/admin/user-management" class="btn btn-sm btn-danger">Manage Users</a>
                    </div>
                </div>
            </div>
        <% } %>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>