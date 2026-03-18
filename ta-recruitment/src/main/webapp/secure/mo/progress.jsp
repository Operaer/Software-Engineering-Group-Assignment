<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Recruitment Progress (Demo)</h2>
            <p class="text-muted">View the current recruitment status for all positions here.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/mo/dashboard.jsp">Back to MO Dashboard</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <div class="row">
                <div class="col-md-4 mb-3">
                    <div class="card">
                        <div class="card-body">
                            <h6>Total Positions</h6>
                            <p class="display-6">8</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 mb-3">
                    <div class="card">
                        <div class="card-body">
                            <h6>Completed</h6>
                            <p class="display-6">3</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 mb-3">
                    <div class="card">
                        <div class="card-body">
                            <h6>In Progress</h6>
                            <p class="display-6">5</p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="mt-4">
                <h6>Recent Updates</h6>
                <ul class="list-group">
                    <li class="list-group-item">2026-03-15: Added "Data Structures TA" position.</li>
                    <li class="list-group-item">2026-03-14: Updated "Operating Systems Tutor" application status to Shortlisted.</li>
                    <li class="list-group-item">2026-03-10: Marked "English Writing Reviewer" as completed.</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
