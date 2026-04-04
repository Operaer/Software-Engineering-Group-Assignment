<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    Map<String, User> users = (Map<String, User>) request.getAttribute("users");
    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>User Management</h2>
            <p class="text-muted">View and manage real user accounts stored in the system.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <% if (success != null && !success.isBlank()) { %>
        <div class="alert alert-success mt-3"><%= success %></div>
    <% } %>

    <% if (error != null && !error.isBlank()) { %>
        <div class="alert alert-danger mt-3"><%= error %></div>
    <% } %>

    <div class="card mt-4">
        <div class="card-header">
            <strong>Create New User</strong>
        </div>
        <div class="card-body">
            <form method="post" action="<%= request.getContextPath() %>/secure/admin/user-management">
                <input type="hidden" name="action" value="create">

                <div class="row g-3">
                    <div class="col-md-3">
                        <label class="form-label" for="username">Username</label>
                        <input class="form-control" type="text" id="username" name="username" placeholder="e.g. ta2">
                    </div>

                    <div class="col-md-3">
                        <label class="form-label" for="email">Email</label>
                        <input class="form-control" type="email" id="email" name="email" placeholder="e.g. ta2@example.com" required>
                    </div>

                    <div class="col-md-2">
                        <label class="form-label" for="password">Password</label>
                        <input class="form-control" type="text" id="password" name="password" required>
                    </div>

                    <div class="col-md-2">
                        <label class="form-label" for="role">Role</label>
                        <select class="form-select" id="role" name="role" required>
                            <option value="TA">TA</option>
                            <option value="MO">MO</option>
                            <option value="ADMIN">ADMIN</option>
                        </select>
                    </div>

                    <div class="col-md-2 d-flex align-items-end">
                        <button class="btn btn-primary w-100" type="submit">Create User</button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <table class="table table-hover align-middle">
                <thead>
                <tr>
                    <th>Username</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th class="text-end">Actions</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (users == null || users.isEmpty()) {
                %>
                    <tr>
                        <td colspan="5" class="text-center text-muted">No users found.</td>
                    </tr>
                <%
                    } else {
                        for (Map.Entry<String, User> entry : users.entrySet()) {
                            User user = entry.getValue();
                            String badgeClass = user.isActive() ? "status-accepted" : "status-rejected";
                %>
                    <tr>
                        <td><%= user.getUsername() %></td>
                        <td><%= user.getEmail() %></td>
                        <td><%= user.getRole() %></td>
                        <td>
                            <span class="badge <%= badgeClass %>"><%= user.getStatusText() %></span>
                        </td>
                        <td class="text-end">
                            <div class="d-inline-flex gap-2 flex-wrap justify-content-end">

                                <form method="post" action="<%= request.getContextPath() %>/secure/admin/user-management" style="display:inline;">
                                    <input type="hidden" name="action" value="toggleStatus">
                                    <input type="hidden" name="email" value="<%= user.getEmail() %>">
                                    <button type="submit" class="btn btn-sm btn-warning">
                                        <%= user.isActive() ? "Disable" : "Enable" %>
                                    </button>
                                </form>

                                <form method="post" action="<%= request.getContextPath() %>/secure/admin/user-management" style="display:inline;">
                                    <input type="hidden" name="action" value="resetPassword">
                                    <input type="hidden" name="email" value="<%= user.getEmail() %>">
                                    <button type="submit" class="btn btn-sm btn-secondary">Reset Password</button>
                                </form>

                                <form method="post" action="<%= request.getContextPath() %>/secure/admin/user-management" style="display:inline;"
                                      onsubmit="return confirm('Delete this user?');">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="email" value="<%= user.getEmail() %>">
                                    <button type="submit" class="btn btn-sm btn-danger">Delete</button>
                                </form>

                            </div>
                        </td>
                    </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>