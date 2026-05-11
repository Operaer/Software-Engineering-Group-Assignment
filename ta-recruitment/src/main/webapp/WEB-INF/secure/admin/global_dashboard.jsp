<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2>Admin Global Recruitment Dashboard</h2>
            <p class="text-muted mb-0">School-wide recruitment metrics are calculated from the latest positions and applications.</p>
            <small class="text-muted">${syncMessage} Last synced: ${lastSyncedAt}</small>
        </div>
        <div class="d-flex gap-2">
            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/secure/admin/global-dashboard?module=${selectedModule}">Refresh Sync</a>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <div class="row g-3 mb-4">
        <div class="col-md-3">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <p class="text-muted mb-1">Total TA Positions</p>
                    <h3 class="mb-0">${stats.totalPositions}</h3>
                    <small class="text-muted">Open: ${stats.openPositions}, Closed: ${stats.closedPositions}, Archived: ${stats.archivedPositions}</small>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <p class="text-muted mb-1">Total Applications</p>
                    <h3 class="mb-0">${stats.totalApplications}</h3>
                    <small class="text-muted">All submitted TA applications</small>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <p class="text-muted mb-1">Accepted Applications</p>
                    <h3 class="mb-0">${stats.acceptedApplications}</h3>
                    <small class="text-muted">Used as completed recruitment records</small>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <p class="text-muted mb-1">Completion Rate</p>
                    <h3 class="mb-0"><fmt:formatNumber value="${stats.completionRate}" maxFractionDigits="1" />%</h3>
                    <small class="text-muted">Accepted applications / total applications</small>
                </div>
            </div>
        </div>
    </div>

    <div class="card shadow-sm">
        <div class="card-header bg-white d-flex justify-content-between align-items-center flex-wrap gap-2">
            <h5 class="mb-0">Applicants per Module</h5>
            <form class="d-flex align-items-center gap-2 mb-0" method="get" action="${pageContext.request.contextPath}/secure/admin/global-dashboard">
                <label for="module" class="form-label mb-0 small text-muted">Module</label>
                <select class="form-select form-select-sm" id="module" name="module" style="width: 150px;">
                    <option value="ALL" ${selectedModule == 'ALL' ? 'selected' : ''}>All Modules</option>
                    <c:forEach var="moduleCode" items="${moduleOptions}">
                        <option value="${moduleCode}" ${selectedModule == moduleCode ? 'selected' : ''}>${moduleCode}</option>
                    </c:forEach>
                </select>
                <button type="submit" class="btn btn-sm btn-primary">Filter</button>
                <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/secure/admin/global-dashboard">Reset</a>
            </form>
        </div>
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th>Module Code</th>
                        <th>TA Positions</th>
                        <th>Applicants</th>
                        <th>Accepted</th>
                        <th>Completion Rate</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty stats.moduleMetrics}">
                            <tr>
                                <td colspan="5" class="text-center text-muted py-4">No recruitment data is available yet.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="module" items="${stats.moduleMetrics}">
                                <tr>
                                    <td class="fw-semibold">${module.moduleCode}</td>
                                    <td>${module.positionCount}</td>
                                    <td>${module.applicantCount}</td>
                                    <td>${module.acceptedCount}</td>
                                    <td><fmt:formatNumber value="${module.completionRate}" maxFractionDigits="1" />%</td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <div class="row g-4 mt-1">
        <div class="col-lg-6">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white">
                    <h5 class="mb-0">TA Workload Distribution</h5>
                    <small class="text-muted">Aggregated per TA from accepted applications.</small>
                </div>
                <div class="table-responsive">
                    <table class="table table-striped table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th>TA Email</th>
                                <th>Modules</th>
                                <th>Accepted Positions</th>
                                <th>Total Workload</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty stats.taWorkloadMetrics}">
                                    <tr>
                                        <td colspan="4" class="text-center text-muted py-4">No accepted TA workload data is available yet.</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="ta" items="${stats.taWorkloadMetrics}">
                                        <tr>
                                            <td class="fw-semibold">${ta.taEmail}</td>
                                            <td>${ta.modules}</td>
                                            <td>${ta.acceptedPositions}</td>
                                            <td>${ta.totalWorkload}</td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="col-lg-6">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white">
                    <h5 class="mb-0">Module Workload Distribution</h5>
                    <small class="text-muted">Aggregated per module from hired TAs.</small>
                </div>
                <div class="table-responsive">
                    <table class="table table-striped table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th>Module Code</th>
                                <th>Hired TAs</th>
                                <th>Accepted Positions</th>
                                <th>Total Workload</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty stats.moduleWorkloadMetrics}">
                                    <tr>
                                        <td colspan="4" class="text-center text-muted py-4">No module workload data is available yet.</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="moduleWorkload" items="${stats.moduleWorkloadMetrics}">
                                        <tr>
                                            <td class="fw-semibold">${moduleWorkload.moduleCode}</td>
                                            <td>${moduleWorkload.hiredTAs}</td>
                                            <td>${moduleWorkload.acceptedPositions}</td>
                                            <td>${moduleWorkload.totalWorkload}</td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
