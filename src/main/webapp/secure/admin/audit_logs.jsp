<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    java.util.List entries = (java.util.List) request.getAttribute("entries");
    if (entries == null) {
        try {
            com.bupt.ta.storage.AuditLogStorage storage = new com.bupt.ta.storage.AuditLogStorage(application);
            entries = storage.query(null, null, null, null);
        } catch (Exception ignored) {
            entries = new java.util.ArrayList();
        }
    }
    java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(java.time.ZoneId.systemDefault());
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Audit Logs</h2>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
        </div>
    </div>

        <form class="row g-2 mb-3" method="get" action="<%= request.getContextPath() %>/secure/admin/audit-logs">
            <div class="col-auto">
                <select class="form-select" name="operator">
                    <option value="">All Operators</option>
                    <% java.util.List operatorOptions = (java.util.List) request.getAttribute("operatorOptions");
                       String selectedOperator = request.getParameter("operator");
                       if (operatorOptions != null) {
                           for (Object op : operatorOptions) {
                               String operatorValue = op == null ? "" : op.toString(); %>
                    <option value="<%= operatorValue %>" <%= operatorValue.equals(selectedOperator) ? "selected" : "" %>><%= operatorValue %></option>
                    <%     }
                       }
                    %>
                </select>
            </div>
            <div class="col-auto">
                <select class="form-select" name="actionType">
                    <option value="">All Action Types</option>
                    <% java.util.List actionTypeOptions = (java.util.List) request.getAttribute("actionTypeOptions");
                       String selectedActionType = request.getParameter("actionType");
                       if (actionTypeOptions != null) {
                           for (Object at : actionTypeOptions) {
                               String actionValue = at == null ? "" : at.toString(); %>
                    <option value="<%= actionValue %>" <%= actionValue.equalsIgnoreCase(selectedActionType == null ? "" : selectedActionType) ? "selected" : "" %>><%= actionValue %></option>
                    <%     }
                       }
                    %>
                </select>
            </div>
            <div class="col-auto">
                <input class="form-control" name="from" placeholder="From (ISO instant)" value="<%= request.getParameter("from") == null ? "" : request.getParameter("from") %>">
            </div>
            <div class="col-auto">
                <input class="form-control" name="to" placeholder="To (ISO instant)" value="<%= request.getParameter("to") == null ? "" : request.getParameter("to") %>">
            </div>
            <div class="col-auto">
                <button class="btn btn-primary">Filter</button>
            </div>
        </form>

        <div class="card mt-1">
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
                <% for (Object o : entries) {
                       com.bupt.ta.model.AuditLogEntry e = (com.bupt.ta.model.AuditLogEntry) o; %>
                    <tr>
                        <td><%= e.getTimestamp() == null ? "" : fmt.format(e.getTimestamp()) %></td>
                        <td><%= e.getOperator() %></td>
                        <td><%= e.getActionType() %></td>
                        <td><%= e.getDescription() %> <%= e.getTarget() != null ? (" (" + e.getTarget() + ")") : "" %></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
