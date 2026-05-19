<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Objects" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    java.util.List<Application> applications = (java.util.List<Application>) request.getAttribute("applications");
    if (applications == null) {
        applications = java.util.Collections.emptyList();
    }
    java.util.List<Job> availableJobs = (java.util.List<Job>) request.getAttribute("availableJobs");
    if (availableJobs == null) {
        availableJobs = java.util.Collections.emptyList();
    }

    String courseKeyword = request.getParameter("courseKeyword");
    String skillKeyword = request.getParameter("skillKeyword");
    String minimumGpaRaw = request.getParameter("minimumGpa");
    Double minimumGpaValue = null;
    try {
        minimumGpaValue = minimumGpaRaw == null || minimumGpaRaw.isBlank()
                ? null
                : Double.parseDouble(minimumGpaRaw.trim());
    } catch (NumberFormatException ignored) {
        minimumGpaValue = null;
    }

    List<Job> displayJobs = new ArrayList<>();
    for (Job job : availableJobs) {
        String moduleCode = job.getModuleCode() == null ? "" : job.getModuleCode();
        String requirements = job.getRequirements() == null ? "" : job.getRequirements();
        boolean matchCourse = courseKeyword == null || courseKeyword.isBlank()
                || moduleCode.toLowerCase(Locale.ROOT).contains(courseKeyword.trim().toLowerCase(Locale.ROOT));
        boolean matchSkill = skillKeyword == null || skillKeyword.isBlank()
                || requirements.toLowerCase(Locale.ROOT).contains(skillKeyword.trim().toLowerCase(Locale.ROOT));
        double extractedGpa = 0.0;
        Matcher gpaMatcher = Pattern.compile("(?i)gpa\\s*(?:>=|>|at least|minimum|min)?\\s*([0-4](?:\\.\\d+)?)").matcher(requirements);
        if (gpaMatcher.find()) {
            try {
                extractedGpa = Double.parseDouble(gpaMatcher.group(1));
            } catch (NumberFormatException ignored) {
                extractedGpa = 0.0;
            }
        }
        boolean matchGpa = minimumGpaValue == null || extractedGpa <= minimumGpaValue;
        if (matchCourse && matchSkill && matchGpa) {
            displayJobs.add(job);
        }
    }
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>My Applications</h2>
            <p class="text-muted">View your submitted applications and current status.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <c:if test="${not empty success}">
        <div class="alert alert-success">${success}</div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <!-- Available Positions -->
    <div class="card mt-3">
        <div class="card-header">
            <h5>Available Positions</h5>
        </div>
        <div class="card-body">
            <form class="row g-2 mb-4" method="get" action="<%= request.getContextPath() %>/secure/ta/applications">
                <div class="col-md-4">
                    <input type="text" class="form-control" name="courseKeyword" value="${courseKeyword}" placeholder="Course keyword">
                </div>
                <div class="col-md-4">
                    <input type="text" class="form-control" name="skillKeyword" value="${skillKeyword}" placeholder="Skill keyword">
                </div>
                <div class="col-md-2">
                    <input type="number" step="0.01" min="0" max="4.0" class="form-control" name="minimumGpa" value="${minimumGpa}" placeholder="Min GPA">
                </div>
                <div class="col-md-2 d-flex gap-2">
                    <button type="submit" class="btn btn-primary w-100">Filter</button>
                    <a class="btn btn-outline-secondary w-100" href="<%= request.getContextPath() %>/secure/ta/applications">Reset</a>
                </div>
            </form>
            <div class="row">
                <c:forEach var="job" items="<%= displayJobs %>">
                    <div class="col-md-6 mb-3">
                        <div class="card">
                            <div class="card-body">
                                <h6 class="card-title">${job.title}</h6>
                                <p class="card-text">
                                    <strong>Module Code:</strong> ${job.moduleCode}<br>
                                    <strong>Workload:</strong> ${job.workload}<br>
                                    <strong>Deadline:</strong> ${job.deadline}<br>
                                    <strong>Requirements:</strong> ${job.requirements}
                                </p>
                                <div class="d-flex gap-2">
                                    <a class="btn btn-outline-secondary btn-sm"
                                       href="<%= request.getContextPath() %>/secure/ta/positions/${job.id}">
                                        View Details
                                    </a>
                                <%
                                    boolean hasApplied = false;
                                    Job currentJob = (Job) pageContext.getAttribute("job");
                                    for (Application app : applications) {
                                        if (currentJob != null && Objects.equals(app.getPositionId(), currentJob.getId())) {
                                            hasApplied = true;
                                            break;
                                        }
                                    }
                                    if (!hasApplied) {
                                %>
                                <form method="post" action="<%= request.getContextPath() %>/secure/ta/applications">
                                    <input type="hidden" name="jobId" value="${job.id}">
                                    <button type="submit" class="btn btn-primary btn-sm">Apply</button>
                                </form>
                                <%
                                    } else {
                                %>
                                <span class="text-muted">Already Applied</span>
                                <%
                                    }
                                %>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
                <% if (displayJobs.isEmpty()) { %>
                    <div class="col-12">
                        <p class="text-muted">No available positions found.</p>
                    </div>
                <% } %>
            </div>
        </div>
    </div>

    <!-- My Applications -->
    <div class="card mt-3">
        <div class="card-header">
            <h5>My Applications</h5>
        </div>
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
