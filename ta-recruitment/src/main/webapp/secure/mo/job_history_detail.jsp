<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.JobHistoryEntry" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    Job job = (Job) request.getAttribute("job");
    JobHistoryEntry entry = (JobHistoryEntry) request.getAttribute("historyEntry");
    if (job == null || entry == null) {
        response.sendRedirect(request.getContextPath() + "/secure/mo/manage-job");
        return;
    }
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>History Detail</h2>
            <p class="text-muted">Full change details for <strong><%= job.getTitle() %></strong>.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/mo/manage-job?action=history&jobId=<%= job.getId() %>">Back to History</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <dl class="row">
                <dt class="col-sm-3">Changed At</dt>
                <dd class="col-sm-9"><%= entry.getChangedAt() %></dd>

                <dt class="col-sm-3">Changed By</dt>
                <dd class="col-sm-9"><%= entry.getChangedBy() %></dd>

                <dt class="col-sm-3">Action</dt>
                <dd class="col-sm-9"><%= entry.getAction() %></dd>
            </dl>

            <div class="mt-3">
                <h5>Change Details</h5>
                <pre class="bg-light p-3 border rounded"><%= entry.getDetails() != null ? entry.getDetails() : "No additional details." %></pre>
            </div>

            <% if (entry.getSnapshot() != null && !entry.getSnapshot().isBlank()) { %>
                <div class="mt-4">
                    <h5>Snapshot</h5>
                    <pre class="bg-light p-3 border rounded"><%= entry.getSnapshot() %></pre>
                </div>
            <% } %>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
