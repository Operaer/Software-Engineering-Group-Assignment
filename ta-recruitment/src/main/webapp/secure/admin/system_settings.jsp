<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>System Settings (Demo)</h2>
            <p class="text-muted">Adjust demo settings within the application (real production would connect to a config service or database).</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/admin/dashboard.jsp">Back to Admin Dashboard</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <form>
                <div class="mb-3">
                    <label class="form-label">Site Name</label>
                    <input type="text" class="form-control" value="BUPT TA Recruitment">
                </div>
                <div class="mb-3">
                    <label class="form-label">Max Concurrent Logins</label>
                    <input type="number" class="form-control" value="100">
                </div>
                <div class="mb-3">
                    <label class="form-label">Email Notifications</label>
                    <select class="form-select">
                        <option selected>Enabled</option>
                        <option>Disabled</option>
                    </select>
                </div>
                <button type="button" class="btn btn-primary">Save Settings (Demo)</button>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
