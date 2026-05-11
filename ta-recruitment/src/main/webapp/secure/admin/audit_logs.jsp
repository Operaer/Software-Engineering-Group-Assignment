<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.OperationLogEntry" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    java.util.List<OperationLogEntry> logs = (java.util.List<OperationLogEntry>) request.getAttribute("logs");
    String operatorValue = request.getAttribute("operator") != null ? (String) request.getAttribute("operator") : "";
    String actionTypeValue = request.getAttribute("actionType") != null ? (String) request.getAttribute("actionType") : "";
    String fromValue = request.getAttribute("from") != null ? (String) request.getAttribute("from") : "";
    String toValue = request.getAttribute("to") != null ? (String) request.getAttribute("to") : "";
    String filterError = request.getAttribute("filterError") != null ? (String) request.getAttribute("filterError") : null;
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Audit Logs</h2>
            <p class="text-muted">Review read-only system operation logs for compliance and traceability.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <form class="row g-3 mb-4" method="get" action="<%= request.getContextPath() %>/secure/admin/audit-logs">
                <div class="col-md-3">
                    <label class="form-label">Operator</label>
                    <input type="text" name="operator" class="form-control" value="<%= operatorValue %>" placeholder="admin@example.com">
                </div>
                <div class="col-md-3">
                    <label class="form-label">Action Type</label>
                    <input type="text" name="actionType" class="form-control" value="<%= actionTypeValue %>" placeholder="User Management">
                </div>
                <div class="col-md-3">
                    <label class="form-label">From</label>
                    <input type="text" name="from" class="form-control" value="<%= fromValue %>" placeholder="yyyy/MM/dd HH:mm">
                </div>
                <div class="col-md-3">
                    <label class="form-label">To</label>
                    <input type="text" name="to" class="form-control" value="<%= toValue %>" placeholder="yyyy/MM/dd HH:mm">
                </div>
                <div class="col-12">
                    <button type="submit" class="btn btn-primary">Filter</button>
                    <a href="<%= request.getContextPath() %>/secure/admin/audit-logs" class="btn btn-outline-secondary">Reset</a>
                </div>
                <% if (filterError != null) { %>
                    <div class="col-12 mt-2 text-danger"><%= filterError %></div>
                <% } %>
            </form>

            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                    <tr>
                        <th>Timestamp</th>
                        <th>Operator</th>
                        <th>Action Type</th>
                        <th>Target</th>
                        <th>Details</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if (logs == null || logs.isEmpty()) { %>
                        <tr>
                            <td colspan="5" class="text-center text-muted">No log entries match the current filter.</td>
                        </tr>
                    <% } else {
                        for (OperationLogEntry log : logs) {
                    %>
                        <tr>
                            <td><%= log.getTimestamp() %></td>
                            <td><%= log.getOperator() %></td>
                            <td><%= log.getActionType() %></td>
                            <td><%= log.getTarget() %></td>
                            <td><%= log.getDetails() %></td>
                        </tr>
                    <% }
                    } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
