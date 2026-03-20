<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Admin Dashboard</h2>
            <p class="text-muted">Welcome, <strong><%= currentUser.getEmail() %></strong>!</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
    </div>

    <div class="row mt-4">
        <div class="col-md-4">
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">User Management</h5>
                    <p class="card-text">View/edit all user accounts, roles, and active status.</p>
                    <a href="<%= request.getContextPath() %>/secure/admin/user_management.jsp" class="btn btn-sm btn-primary">Go to User Management</a>
                </div>
            </div>

            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">System Settings</h5>
                    <p class="card-text">Adjust site settings (demo), such as notification toggles and concurrency limits.</p>
                    <a href="<%= request.getContextPath() %>/secure/admin/system_settings.jsp" class="btn btn-sm btn-primary">Go to System Settings</a>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Audit Logs</h5>
                    <p class="card-text">Review critical operation logs to track system changes.</p>
                    <a href="<%= request.getContextPath() %>/secure/admin/audit_logs.jsp" class="btn btn-sm btn-primary">View Audit Logs</a>
                </div>
            </div>
        </div>

        <div class="col-md-8">
            <div class="card mb-4">
                <div class="card-body">
                    <h5 class="card-title">核心指标</h5>
                    <div class="row text-center">
                        <div class="col-4">
                            <div class="mb-1 text-muted">Total Positions</div>
                            <div class="h3">42</div>
                        </div>
                        <div class="col-4">
                            <div class="mb-1 text-muted">Total Applications</div>
                            <div class="h3">231</div>
                        </div>
                        <div class="col-4">
                            <div class="mb-1 text-muted">TA Hired</div>
                            <div class="h3">63</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Workload Alerts</h5>
                    <p class="text-muted">The system identifies potential overloads based on TA assigned hours (sample data).</p>
                    <table class="table table-hover">
                        <thead>
                        <tr>
                            <th>TA</th>
                            <th>Assigned Hours</th>
                            <th>Safe Limit</th>
                            <th>Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>Zhang San</td>
                            <td>22</td>
                            <td>20</td>
                            <td><span class="badge status-rejected">Overloaded</span></td>
                        </tr>
                        <tr>
                            <td>Li Si</td>
                            <td>15</td>
                            <td>20</td>
                            <td><span class="badge status-accepted">Normal</span></td>
                        </tr>
                        <tr>
                            <td>Wang Wu</td>
                            <td>19</td>
                            <td>20</td>
                            <td><span class="badge status-shortlisted">Near Limit</span></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
