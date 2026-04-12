<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.util.List" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    if (jobs == null) {
        jobs = java.util.Collections.emptyList();
    }
    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Manage TA Positions</h2>
            <p class="text-muted">Edit details, archive completed positions, and review audit history.</p>
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
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead>
                    <tr>
                        <th>Position</th>
                        <th>Module</th>
                        <th>Workload</th>
                        <th>Deadline</th>
                        <th>Status</th>
                        <th>Updated</th>
                        <th class="text-end">Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (Job job : jobs) {
                        String effectiveStatus = job.getStatus();
                        if (!Job.STATUS_ARCHIVED.equals(job.getStatus()) && job.getDeadline().isBefore(java.time.LocalDate.now())) {
                            effectiveStatus = Job.STATUS_CLOSED;
                        }
                    %>
                    <tr>
                        <td><%= job.getTitle() %></td>
                        <td><%= job.getModuleCode() %></td>
                        <td><%= job.getWorkload() %></td>
                        <td><%= job.getDeadline() %></td>
                        <td><span class="badge <%= Job.STATUS_ARCHIVED.equals(effectiveStatus) ? "status-rejected" : Job.STATUS_CLOSED.equals(effectiveStatus) ? "status-accepted" : Job.STATUS_OPEN.equals(effectiveStatus) ? "status-open" : "status-pending" %>"><%= effectiveStatus %></span></td>
                        <td><%= job.getUpdatedAt() != null ? job.getUpdatedAt() : job.getPostedAt() %></td>
                        <td class="text-end">
                            <a class="btn btn-sm btn-primary me-1" href="<%= request.getContextPath() %>/secure/mo/manage-job?action=view&jobId=<%= job.getId() %>">View</a>
                            <a class="btn btn-sm btn-secondary me-1" href="<%= request.getContextPath() %>/secure/mo/manage-job?action=edit&jobId=<%= job.getId() %>">Edit</a>
                            <a class="btn btn-sm btn-info me-1" href="<%= request.getContextPath() %>/secure/mo/manage-job?action=history&jobId=<%= job.getId() %>">History</a>
                            <% if (!Job.STATUS_ARCHIVED.equals(job.getStatus())) { %>
                                <form class="d-inline" method="post" action="<%= request.getContextPath() %>/secure/mo/manage-job">
                                    <input type="hidden" name="action" value="archive" />
                                    <input type="hidden" name="jobId" value="<%= job.getId() %>" />
                                    <button type="submit" class="btn btn-sm btn-danger" onclick="return confirm('Archive this position and stop new applications?');">Archive</button>
                                </form>
                            <% } %>
                        </td>
                    </tr>
                    <% } %>
                    <% if (jobs.isEmpty()) { %>
                    <tr>
                        <td colspan="7" class="text-center text-muted">No positions found. Create one in the Post Position page.</td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
