<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.JobHistoryEntry" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    Job job = (Job) request.getAttribute("job");
    List<JobHistoryEntry> history = (List<JobHistoryEntry>) request.getAttribute("history");
    if (history == null) {
        history = java.util.Collections.emptyList();
    }
    if (job == null) {
        response.sendRedirect(request.getContextPath() + "/secure/mo/manage-job");
        return;
    }
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Audit History</h2>
            <p class="text-muted">Review the change log for <strong><%= job.getTitle() %></strong>.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/mo/manage-job">Back to Manage Positions</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead>
                    <tr>
                        <th>When</th>
                        <th>Changed By</th>
                        <th>Action</th>
                        <th>Details</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="entry" items="${history}">
                        <tr>
                            <td>${entry.changedAt}</td>
                            <td>${entry.changedBy}</td>
                            <td>${entry.action}</td>
                            <td>${entry.details}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty history}">
                        <tr>
                            <td colspan="4" class="text-center text-muted">No history recorded for this position yet.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
