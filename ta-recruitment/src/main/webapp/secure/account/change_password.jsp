<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
%>

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-7">
            <div class="card shadow-sm">
                <div class="card-body">
                    <h2 class="mb-3">Change Password</h2>
                    <p class="text-muted">
                        Update your account password here. This function is available to all logged-in users.
                    </p>

                    <% if (success != null && !success.isBlank()) { %>
                        <div class="alert alert-success"><%= success %></div>
                    <% } %>

                    <% if (error != null && !error.isBlank()) { %>
                        <div class="alert alert-danger"><%= error %></div>
                    <% } %>

                    <form method="post" action="<%= request.getContextPath() %>/secure/account/change-password">
                        <div class="mb-3">
                            <label for="currentPassword" class="form-label">Current Password</label>
                            <input type="password" class="form-control" id="currentPassword" name="currentPassword" required>
                        </div>

                        <div class="mb-3">
                            <label for="newPassword" class="form-label">New Password</label>
                            <input type="password" class="form-control" id="newPassword" name="newPassword" required>
                        </div>

                        <div class="mb-3">
                            <label for="confirmPassword" class="form-label">Confirm New Password</label>
                            <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
                        </div>

                        <div class="d-flex gap-2">
                            <button type="submit" class="btn btn-primary">Change Password</button>
                            <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-outline-secondary">Back to Dashboard</a>
                        </div>
                    </form>

                    <div class="mt-4 text-muted small">
                        Password rules in this version:
                        <ul class="mb-0">
                            <li>All fields are required.</li>
                            <li>New password must be at least 6 characters.</li>
                            <li>New password must be different from the current password.</li>
                            <li>The confirmation password must match.</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>