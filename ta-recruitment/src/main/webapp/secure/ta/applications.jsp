<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    java.util.List<Application> applications = (java.util.List<Application>) request.getAttribute("applications");
    if (applications == null) {
        applications = java.util.Collections.emptyList();
    }
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>My Applications</h2>
            <p class="text-muted">View submitted applications and their current status.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <c:if test="${not empty success}">
        <div class="alert alert-success">${success}</div>
    </c:if>

    <div class="card mt-3">
        <div class="card-body">
            <div class="row g-2 mb-3">
                <div class="col-md-4">
                    <input id="appFilterKeyword" type="text" class="form-control" placeholder="Keyword (position/status)" />
                </div>
                <div class="col-md-3">
                    <select id="appFilterStatus" class="form-select">
                        <option value="">All statuses</option>
                        <option value="Pending">Pending</option>
                        <option value="Shortlisted">Shortlisted</option>
                        <option value="Accepted">Accepted</option>
                        <option value="Rejected">Rejected</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <button id="appFilterClear" class="btn btn-outline-secondary w-100">Clear</button>
                </div>
            </div>

            <table id="appListTable" class="table table-hover">
                <thead>
                <tr>
                    <th>Position</th>
                    <th>Applied At</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="app" items="${applications}">
                    <tr>
                        <td>${app.positionTitle}</td>
                        <td>${app.appliedAt}</td>
                        <td>
                            <span class="badge 
                                ${app.status == 'Pending' ? 'status-pending' : ''} 
                                ${app.status == 'Shortlisted' ? 'status-shortlisted' : ''} 
                                ${app.status == 'Accepted' ? 'status-accepted' : ''} 
                                ${app.status == 'Rejected' ? 'status-rejected' : ''}">
                                ${app.status}
                            </span>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty applications}">
                    <tr>
                        <td colspan="3" class="text-center text-muted">No applications found.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script>
    const appFilterKeyword = document.getElementById('appFilterKeyword');
    const appFilterStatus = document.getElementById('appFilterStatus');
    const appFilterClear = document.getElementById('appFilterClear');
    const appListTable = document.getElementById('appListTable');

    function applyAppFilters() {
        const keyword = appFilterKeyword.value.trim().toLowerCase();
        const status = appFilterStatus.value;

        const rows = appListTable.querySelectorAll('tbody tr');
        rows.forEach(row => {
            const position = row.cells[0].textContent.toLowerCase();
            const rowStatus = row.cells[2].textContent.trim();

            const matchKeyword = keyword === '' || position.includes(keyword) || rowStatus.toLowerCase().includes(keyword);
            const matchStatus = status === '' || rowStatus === status;

            row.style.display = (matchKeyword && matchStatus) ? '' : 'none';
        });
    }

    appFilterKeyword.addEventListener('input', applyAppFilters);
    appFilterStatus.addEventListener('change', applyAppFilters);
    appFilterClear.addEventListener('click', () => {
        appFilterKeyword.value = '';
        appFilterStatus.value = '';
        applyAppFilters();
    });
</script>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
