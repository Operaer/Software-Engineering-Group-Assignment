<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    Job job = (Job) request.getAttribute("job");
    if (job == null) {
        response.sendRedirect(request.getContextPath() + "/secure/mo/manage-job");
        return;
    }
    Boolean canRollback = (Boolean) request.getAttribute("canRollback");
    if (canRollback == null) {
        canRollback = false;
    }
    String status = job.getStatus();
    if (!Job.STATUS_ARCHIVED.equals(status) && job.getDeadline().isBefore(java.time.LocalDate.now())) {
        status = Job.STATUS_CLOSED;
    }
    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Position Details</h2>
            <p class="text-muted">Review the published position and manage its lifecycle.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/mo/manage-job">Back to Manage Positions</a>
        </div>
    </div>

    <% if (error != null) { %>
        <div class="alert alert-danger mt-3"><%= error %></div>
    <% } %>
    <% if (success != null) { %>
        <div class="alert alert-success mt-3"><%= success %></div>
    <% } %>

    <div class="card mt-4">
        <div class="card-body">
            <div class="row">
                <div class="col-md-8">
                    <h4><%= job.getTitle() %></h4>
                    <p class="text-muted"><strong>Module:</strong> <%= job.getModuleCode() %></p>
                    <p><strong>Workload:</strong> <%= job.getWorkload() %></p>
                    <p><strong>Deadline:</strong> <%= job.getDeadline() %></p>
                    <p><strong>Requirements:</strong><br><%= job.getRequirements() %></p>
                </div>
                <div class="col-md-4">
                    <ul class="list-group">
                        <li class="list-group-item"><strong>Status:</strong> <span class="badge <%= Job.STATUS_ARCHIVED.equals(status) ? "status-rejected" : Job.STATUS_CLOSED.equals(status) ? "status-accepted" : Job.STATUS_OPEN.equals(status) ? "status-open" : "status-pending" %>"><%= status %></span></li>
                        <li class="list-group-item"><strong>Posted By:</strong> <%= job.getPostedBy() %></li>
                        <li class="list-group-item"><strong>Posted At:</strong> <%= job.getPostedAt() %></li>
                        <li class="list-group-item"><strong>Updated At:</strong> <%= job.getUpdatedAt() != null ? job.getUpdatedAt() : job.getPostedAt() %></li>
                    </ul>
                </div>
            </div>

            <div class="mt-4">
                <a class="btn btn-primary me-2" href="<%= request.getContextPath() %>/secure/mo/manage-job?action=edit&jobId=<%= job.getId() %>">Edit</a>
                <form class="d-inline" action="<%= request.getContextPath() %>/secure/mo/manage-job" method="post" onsubmit="return confirm('Rollback to the last saved version?');">
                    <input type="hidden" name="action" value="rollback" />
                    <input type="hidden" name="jobId" value="<%= job.getId() %>" />
                    <button type="submit" class="btn btn-warning" <%= canRollback ? "" : "disabled" %>>Rollback</button>
                </form>
                <a class="btn btn-info ms-2" href="<%= request.getContextPath() %>/secure/mo/manage-job?action=history&jobId=<%= job.getId() %>">View History</a>
            </div>

            <% if (!canRollback) { %>
                <div class="alert alert-secondary mt-3">No previous version available for rollback.</div>
            <% } %>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
