<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Post TA Position</h2>
            <p class="text-muted">MO can fill in job details and post to the system here.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
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
            <form action="<%= request.getContextPath() %>/post-job" method="post">
                <div class="mb-3">
                    <label for="title" class="form-label">Position Title *</label>
                    <input type="text" class="form-control" id="title" name="title" placeholder="e.g., Computer Networks TA" required>
                </div>
                <div class="mb-3">
                    <label for="moduleCode" class="form-label">Module Code *</label>
                    <input type="text" class="form-control" id="moduleCode" name="moduleCode" placeholder="e.g., CS301" required>
                </div>
                <div class="mb-3">
                    <label for="workload" class="form-label">Workload *</label>
                    <input type="text" class="form-control" id="workload" name="workload" placeholder="e.g., 10 hours per week" required>
                </div>
                <div class="mb-3">
                    <label for="deadline" class="form-label">Deadline *</label>
                    <input type="text" class="form-control" id="deadline" name="deadline" placeholder="YYYY-MM-DD" pattern="\d{4}-\d{2}-\d{2}" required>
                    <div class="form-text">Please enter the date in YYYY-MM-DD format.</div>
                </div>
                <div class="mb-3">
                    <label for="requirements" class="form-label">Requirements *</label>
                    <textarea class="form-control" id="requirements" name="requirements" rows="4" placeholder="Briefly describe duties and requirements" required></textarea>
                </div>
                <button type="submit" class="btn btn-primary">Post Position</button>
            </form>
        </div>
    </div>
</div>

<script>
    // Client-side deadline validation
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
