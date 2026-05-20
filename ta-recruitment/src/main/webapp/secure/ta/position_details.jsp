<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    Job job = (Job) request.getAttribute("job");
    if (job == null) {
        response.sendRedirect(request.getContextPath() + "/secure/ta/positions");
        return;
    }
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Position Details</h2>
            <p class="text-muted">Review the full TA position before applying.</p>
        </div>
        <div class="d-flex gap-2">
            <a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/secure/ta/applications">My Applications</a>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/ta/positions">Back to Positions</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <div class="row">
                <div class="col-md-8">
                    <h4><%= job.getTitle() %></h4>
                    <p class="text-muted"><strong>Course:</strong> <%= job.getModuleCode() %></p>
                    <p><strong>Workload:</strong> <%= job.getWorkload() %></p>
                    <p><strong>Deadline:</strong> <%= job.getDeadline() %></p>
                    <p><strong>Core Requirements:</strong><br><%= job.getRequirements() %></p>
                </div>
                <div class="col-md-4">
                    <ul class="list-group">
                        <li class="list-group-item"><strong>Status:</strong> <%= job.getStatus() %></li>
                        <li class="list-group-item"><strong>Posted By:</strong> <%= job.getPostedBy() %></li>
                        <li class="list-group-item"><strong>Posted At:</strong> <%= job.getPostedAt() %></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
