<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Post New Position (Demo)</h2>
            <p class="text-muted">MO can fill position details here and publish them to the system.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/mo/dashboard.jsp">Back to MO Dashboard</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <form>
                <div class="mb-3">
                    <label class="form-label">Position Title</label>
                    <input type="text" class="form-control" placeholder="e.g., Computer Networks TA">
                </div>
                <div class="mb-3">
                    <label class="form-label">Module</label>
                    <input type="text" class="form-control" placeholder="e.g., CS301">
                </div>
                <div class="mb-3">
                    <label class="form-label">Deadline</label>
                    <input type="date" class="form-control">
                </div>
                <div class="mb-3">
                    <label class="form-label">Position Description</label>
                    <textarea class="form-control" rows="4" placeholder="Briefly describe responsibilities and requirements"></textarea>
                </div>
                <button type="button" class="btn btn-primary">Post Position (Demo)</button>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
