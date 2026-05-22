<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2>Recruitment Progress Dashboard</h2>
            <p class="text-muted mb-0">Track applicant numbers, shortlisting progress, hiring results, and deadline urgency for your positions.</p>
        </div>
        <div class="d-flex gap-2">
            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/secure/mo/progress">Refresh</a>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <div class="row g-3 mb-4">
        <div class="col-md-3">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h6 class="text-muted">Total Positions</h6>
                    <p class="display-6 mb-0">${stats.totalPositions}</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h6 class="text-muted">In Progress</h6>
                    <p class="display-6 mb-0">${stats.inProgressPositions}</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h6 class="text-muted">Completed</h6>
                    <p class="display-6 mb-0">${stats.completedPositions}</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h6 class="text-muted">Urgent Deadlines</h6>
                    <p class="display-6 mb-0">${stats.urgentPositions}</p>
                </div>
            </div>
        </div>
    </div>

    <div class="card mb-4 shadow-sm">
        <div class="card-body">
            <h5 class="card-title mb-3">Overall Application Summary</h5>
            <div class="row text-center">
                <div class="col-md-4 border-end">
                    <h6 class="text-muted">Applicants</h6>
                    <p class="fs-3 mb-0">${stats.totalApplicants}</p>
                </div>
                <div class="col-md-4 border-end">
                    <h6 class="text-muted">Shortlisted</h6>
                    <p class="fs-3 mb-0">${stats.totalShortlisted}</p>
                </div>
                <div class="col-md-4">
                    <h6 class="text-muted">Accepted</h6>
                    <p class="fs-3 mb-0">${stats.totalAccepted}</p>
                </div>
            </div>
        </div>
    </div>

    <div class="card shadow-sm">
        <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                    <h5 class="card-title mb-1">Position Progress</h5>
                    <p class="text-muted mb-0">Counts are recalculated from the latest job and application records each time this page loads.</p>
                </div>
                <input id="progressFilter" type="text" class="form-control w-25" placeholder="Filter by title/module/status">
            </div>

            <c:choose>
                <c:when test="${empty stats.positions}">
                    <div class="alert alert-info mb-0">No positions are available for your account yet.</div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table id="progressTable" class="table table-hover align-middle">
                            <thead>
                            <tr>
                                <th>Position</th>
                                <th>Module</th>
                                <th>Applicants</th>
                                <th>Shortlisted</th>
                                <th>Accepted</th>
                                <th>Deadline</th>
                                <th>Days Left</th>
                                <th>Status</th>
                                <th style="width: 160px;">Progress</th>
                                <th class="text-end">Action</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="position" items="${stats.positions}">
                                <tr>
                                    <td>
                                        <strong>${position.title}</strong><br>
                                        <small class="text-muted">${position.jobStatus}</small>
                                    </td>
                                    <td>${position.moduleCode}</td>
                                    <td>${position.applicantCount}</td>
                                    <td>${position.shortlistedCount}</td>
                                    <td>${position.acceptedCount}</td>
                                    <td>${position.deadline}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${position.daysRemaining < 0}">
                                                <span class="text-danger">Overdue by ${-position.daysRemaining} day(s)</span>
                                            </c:when>
                                            <c:otherwise>
                                                ${position.daysRemaining} day(s)
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><span class="badge ${position.badgeClass}">${position.progressLabel}</span></td>
                                    <td>
                                        <div class="progress" style="height: 10px;">
                                            <div class="progress-bar" role="progressbar" style="width: ${position.progressPercent}%" aria-valuenow="${position.progressPercent}" aria-valuemin="0" aria-valuemax="100"></div>
                                        </div>
                                    </td>
                                    <td class="text-end">
                                        <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/secure/mo/application-management?jobId=${position.jobId}">View Applications</a>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script>
    const progressFilter = document.getElementById('progressFilter');
    const progressTable = document.getElementById('progressTable');

    if (progressFilter && progressTable) {
        progressFilter.addEventListener('input', () => {
            const keyword = progressFilter.value.trim().toLowerCase();
            progressTable.querySelectorAll('tbody tr').forEach(row => {
                row.style.display = row.textContent.toLowerCase().includes(keyword) ? '' : 'none';
            });
        });
    }
</script>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
