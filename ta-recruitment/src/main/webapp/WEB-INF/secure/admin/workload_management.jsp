<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2>Admin TA Workload Management</h2>
            <p class="text-muted mb-0">Track each TA's weekly hours, identify overload risks, and adjust assignments manually.</p>
        </div>
        <div class="d-flex gap-2">
            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/secure/admin/global-dashboard">Admin Dashboard</a>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">${successMessage}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <div class="row gy-2">
                <div class="col-md-4">
                    <p class="mb-1 text-muted">Safe weekly limit</p>
                    <h3 class="mb-0">${safeLimit} hours</h3>
                </div>
                <div class="col-md-8">
                    <p class="mb-1 text-muted">Filter TA workload by email, name, course module, and workload range.</p>
                </div>
            </div>
        </div>
    </div>

    <div class="card shadow-sm mb-4">
        <div class="card-header bg-white">
            <h5 class="mb-0">Filters</h5>
        </div>
        <div class="card-body">
            <form class="row g-3" method="get" action="${pageContext.request.contextPath}/secure/admin/workload-management">
                <div class="col-md-4">
                    <label class="form-label">TA name or email</label>
                    <input class="form-control" name="taFilter" value="${taFilter}" placeholder="Search TA">
                </div>
                <div class="col-md-3">
                    <label class="form-label">Module</label>
                    <select class="form-select" name="module">
                        <option value="ALL" ${selectedModule == 'ALL' ? 'selected' : ''}>All Modules</option>
                        <c:forEach var="code" items="${moduleOptions}">
                            <option value="${code}" ${selectedModule == code ? 'selected' : ''}>${code}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label">Min hours</label>
                    <input type="number" min="0" class="form-control" name="minWorkload" value="${minWorkload}" placeholder="0">
                </div>
                <div class="col-md-2">
                    <label class="form-label">Max hours</label>
                    <input type="number" min="0" class="form-control" name="maxWorkload" value="${maxWorkload}" placeholder="0">
                </div>
                <div class="col-md-1 d-flex align-items-end">
                    <button type="submit" class="btn btn-primary w-100">Apply</button>
                </div>
            </form>
        </div>
    </div>

    <div class="card shadow-sm mb-4">
        <div class="card-header bg-white">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h5 class="mb-0">TA Workload Summary</h5>
                    <small class="text-muted">Sorted by weekly workload. Overloaded TAs are highlighted.</small>
                </div>
                <div>
                    <span class="badge bg-warning text-dark">Safe limit: ${safeLimit}h</span>
                </div>
            </div>
        </div>
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th>TA Name</th>
                        <th>TA Email</th>
                        <th>Modules</th>
                        <th>Accepted Positions</th>
                        <th>Total Workload</th>
                        <th>Warning</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty taMetrics}">
                            <tr>
                                <td colspan="6" class="text-center text-muted py-4">No TA workload data matches the current filters.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="ta" items="${taMetrics}">
                                <tr class="${ta.totalWorkload > safeLimit ? 'table-danger' : ''}">
                                    <td>${ta.taName}</td>
                                    <td>${ta.taEmail}</td>
                                    <td>${ta.modules}</td>
                                    <td>${ta.acceptedPositions}</td>
                                    <td>${ta.totalWorkload}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${ta.totalWorkload > safeLimit}">
                                                <span class="badge bg-danger">Overloaded</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-success">Normal</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <div class="row g-4 mb-4">
        <div class="col-lg-6">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white">
                    <h5 class="mb-0">Module Workload Distribution</h5>
                    <small class="text-muted">Aggregated from accepted TA assignments.</small>
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
                                <c:when test="${empty moduleMetrics}">
                                    <tr>
                                        <td colspan="4" class="text-center text-muted py-4">No module workload data is available yet.</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="module" items="${moduleMetrics}">
                                        <tr>
                                            <td class="fw-semibold">${module.moduleCode}</td>
                                            <td>${module.hiredTAs}</td>
                                            <td>${module.acceptedPositions}</td>
                                            <td>${module.totalWorkload}</td>
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
                    <h5 class="mb-0">Assignment Details</h5>
                    <small class="text-muted">Manual adjustment is applied per accepted TA assignment.</small>
                </div>
                <div class="table-responsive">
                    <table class="table table-striped table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th>TA Email</th>
                                <th>Module</th>
                                <th>Position</th>
                                <th>MO Workload</th>
                                <th>Assigned Hours</th>
                                <th>Effective</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty assignments}">
                                    <tr>
                                        <td colspan="7" class="text-center text-muted py-4">No accepted assignments available for adjustment.</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="assignment" items="${assignments}">
                                        <tr>
                                            <td>${assignment.taEmail}</td>
                                            <td>${assignment.moduleCode}</td>
                                            <td>${assignment.positionTitle}</td>
                                            <td>${assignment.jobWorkload}</td>
                                            <td>
                                                <form method="post" action="${pageContext.request.contextPath}/secure/admin/workload-management" class="d-flex gap-2 align-items-center mb-0">
                                                    <input type="hidden" name="applicationId" value="${assignment.applicationId}" />
                                                    <input type="number" min="0" name="assignedWorkloadHours" value="${assignment.assignedWorkload}" class="form-control form-control-sm" style="width: 80px;" />
                                                    <button type="submit" class="btn btn-sm btn-primary">Save</button>
                                                </form>
                                            </td>
                                            <td>${assignment.effectiveWorkload}</td>
                                            <td>
                                                <c:if test="${assignment.assignedWorkload != null}">
                                                    <span class="badge bg-info text-dark">Overridden</span>
                                                </c:if>
                                            </td>
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
