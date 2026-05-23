<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    List<Job> availableJobs = (List<Job>) request.getAttribute("availableJobs");
    if (availableJobs == null) {
        availableJobs = java.util.Collections.emptyList();
    }
    String courseKeyword = request.getParameter("courseKeyword");
    String skillKeyword = request.getParameter("skillKeyword");

    List<Job> filteredJobs = new ArrayList<>();
    for (Job job : availableJobs) {
        String moduleCode = job.getModuleCode() == null ? "" : job.getModuleCode();
        String requirements = job.getRequirements() == null ? "" : job.getRequirements();
        boolean matchCourse = courseKeyword == null || courseKeyword.isBlank()
                || moduleCode.toLowerCase(Locale.ROOT).contains(courseKeyword.trim().toLowerCase(Locale.ROOT));
        boolean matchSkill = skillKeyword == null || skillKeyword.isBlank()
                || requirements.toLowerCase(Locale.ROOT).contains(skillKeyword.trim().toLowerCase(Locale.ROOT));
        if (matchCourse && matchSkill) {
            filteredJobs.add(job);
        }
    }
    request.setAttribute("availableJobs", filteredJobs);
    request.setAttribute("courseKeyword", courseKeyword);
    request.setAttribute("skillKeyword", skillKeyword);
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Available Positions</h2>
            <p class="text-muted">Browse TA positions that are still open for application.</p>
        </div>
        <div class="d-flex gap-2">
            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/secure/ta/applications">My Applications</a>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <form class="row g-2 mb-4" method="get" action="${pageContext.request.contextPath}/secure/ta/positions">
                <div class="col-md-4">
                    <input type="text" class="form-control" name="courseKeyword" value="${courseKeyword}" placeholder="Course keyword">
                </div>
                <div class="col-md-4">
                    <input type="text" class="form-control" name="skillKeyword" value="${skillKeyword}" placeholder="Skill keyword">
                </div>
                <div class="col-md-4 d-flex gap-2">
                    <button type="submit" class="btn btn-primary w-100">Filter</button>
                    <a class="btn btn-outline-secondary w-100" href="${pageContext.request.contextPath}/secure/ta/positions">Reset</a>
                </div>
            </form>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                    <tr>
                        <th>Position Title</th>
                        <th>Course</th>
                        <th>Deadline</th>
                        <th>Core Requirements</th>
                        <th class="text-end">Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="job" items="${availableJobs}">
                        <tr>
                            <td>${job.title}</td>
                            <td>${job.moduleCode}</td>
                            <td>${job.deadline}</td>
                            <td>${job.requirements}</td>
                            <td class="text-end">
                                <a class="btn btn-sm btn-primary"
                                   href="${pageContext.request.contextPath}/secure/ta/positions/${job.id}">
                                    View Details
                                </a>
                                <form method="post" action="${pageContext.request.contextPath}/secure/ta/quick-apply" style="display: inline;">
                                    <input type="hidden" name="jobId" value="${job.id}">
                                    <button type="submit" class="btn btn-sm btn-success">Quick Apply</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty availableJobs}">
                        <tr>
                            <td colspan="5" class="text-center text-muted">No available positions found.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
