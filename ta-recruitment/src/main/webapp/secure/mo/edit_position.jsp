<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    Job job = (Job) request.getAttribute("job");
    if (job == null) {
        response.sendRedirect(request.getContextPath() + "/secure/mo/manage-job");
        return;
    }
    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
    String status = job.getStatus();
    if (!Job.STATUS_ARCHIVED.equals(status) && job.getDeadline().isBefore(java.time.LocalDate.now())) {
        status = Job.STATUS_CLOSED;
    }
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Edit Position</h2>
            <p class="text-muted">Update workload, deadline, and requirements for this position.</p>
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
            <div class="mb-3">
                <strong>Current Status:</strong>
                <span class="badge <%= Job.STATUS_ARCHIVED.equals(status) ? "status-rejected" : Job.STATUS_CLOSED.equals(status) ? "status-accepted" : "status-pending" %>"><%= status %></span>
            </div>
            <form action="<%= request.getContextPath() %>/secure/mo/manage-job" method="post">
                <input type="hidden" name="action" value="update" />
                <input type="hidden" name="jobId" value="<%= job.getId() %>" />

                <div class="mb-3">
                    <label for="title" class="form-label">Position Title *</label>
                    <input type="text" class="form-control" id="title" name="title" value="<%= job.getTitle() %>" required>
                </div>
                <div class="mb-3">
                    <label for="moduleCode" class="form-label">Module Code *</label>
                    <input type="text" class="form-control" id="moduleCode" name="moduleCode" value="<%= job.getModuleCode() %>" required>
                </div>
                <div class="mb-3">
                    <label for="workload" class="form-label">Workload *</label>
                    <input type="text" class="form-control" id="workload" name="workload" value="<%= job.getWorkload() %>" required>
                </div>
                <div class="mb-3">
                    <label for="deadline" class="form-label">Deadline *</label>
                    <input type="text" class="form-control" id="deadline" name="deadline" value="<%= job.getDeadline() %>" pattern="\d{4}-\d{2}-\d{2}" required>
                    <div class="form-text">Please enter the date in YYYY-MM-DD format.</div>
                </div>
                <div class="mb-3">
                    <label for="requirements" class="form-label">Requirements *</label>
                    <textarea class="form-control" id="requirements" name="requirements" rows="4" required><%= job.getRequirements() %></textarea>
                </div>
                <button type="submit" class="btn btn-primary">Save Changes</button>
            </form>

            <% if (!Job.STATUS_ARCHIVED.equals(job.getStatus())) { %>
            <form class="mt-3" action="<%= request.getContextPath() %>/secure/mo/manage-job" method="post" onsubmit="return confirm('Archive this position and stop new applications?');">
                <input type="hidden" name="action" value="archive" />
                <input type="hidden" name="jobId" value="<%= job.getId() %>" />
                <button type="submit" class="btn btn-danger">Archive Position</button>
            </form>
            <% } %>
        </div>
    </div>
</div>

<script>
    document.getElementById('deadline').addEventListener('change', function() {
        const selectedDate = new Date(this.value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (selectedDate <= today || isNaN(selectedDate.getTime())) {
            alert('Deadline must be a valid future date in YYYY-MM-DD format.');
            this.value = '';
        }
    });
</script>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
