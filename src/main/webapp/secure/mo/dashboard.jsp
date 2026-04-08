<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.storage.JobStorage" %>
<%@ page import="com.bupt.ta.storage.ApplicationStorage" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    JobStorage jobStorage = new JobStorage(application);
    ApplicationStorage appStorage = new ApplicationStorage(application);
    List<Job> jobs = jobStorage.findAll();
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>MO Dashboard</h2>
            <p class="text-muted">Welcome, <strong><%= currentUser.getEmail() %></strong>! This is the Module Organizer (MO) workspace.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
    </div>

    <div class="row mt-4">
        <div class="col-md-4">
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">发布助教职位</h5>
                    <p class="card-text">发布新的助教招聘职位，填写职位详情。</p>
                    <a href="<%= request.getContextPath() %>/post-job" class="btn btn-sm btn-primary">发布职位</a>
                </div>
            </div>

            <div class="card mb-3 border-primary shadow-sm">
                <div class="card-body">
                    <h5 class="card-title text-primary">Manage Posted Positions</h5>
                    <p class="card-text">View, edit, archive or inspect history for already published positions.</p>
                    <a href="<%= request.getContextPath() %>/secure/mo/manage-job" class="btn btn-sm btn-primary">Go to Position Management</a>
                </div>
            </div>

            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">View Applications</h5>
                    <p class="card-text">Browse applicants for each position and update screening status.</p>
                    <a href="<%= request.getContextPath() %>/secure/mo/application_list.jsp" class="btn btn-sm btn-primary">View Application List</a>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Recruitment Progress</h5>
                    <p class="card-text">Track recruitment progress and number of hires for each position.</p>
                    <a href="<%= request.getContextPath() %>/secure/mo/progress.jsp" class="btn btn-sm btn-primary">View Progress</a>
                </div>
            </div>
        </div>

        <div class="col-md-8">
            <div class="card mb-4">
                <div class="card-body">
                    <h5 class="card-title">Recently Posted Positions</h5>
                    <p class="text-muted">Showing your most recent postings and current application counts.</p>

                    <div class="row g-2 mb-3">
                        <div class="col-md-4">
                            <input id="jobFilterKeyword" type="text" class="form-control" placeholder="Keyword (position/module)" />
                        </div>
                        <div class="col-md-3">
                            <select id="jobFilterStatus" class="form-select">
                                <option value="">All statuses</option>
                                <option value="Open">Open</option>
                                <option value="Closed">Closed</option>
                                <option value="Archived">Archived</option>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <button id="jobFilterClear" class="btn btn-outline-secondary w-100">Clear</button>
                        </div>
                    </div>

                    <table id="jobListTable" class="table table-hover">
                        <thead>
                        <tr>
                            <th>Position</th>
                            <th>Module</th>
                            <th>Applicants</th>
                            <th>Deadline</th>
                            <th>Status</th>
                            <th class="text-end">Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% for (Job job : jobs) { 
                            int applicantCount = appStorage.findAll().stream()
                                .mapToInt(app -> app.getPositionId().equals(job.getId()) ? 1 : 0)
                                .sum();
                            String status = Job.STATUS_ARCHIVED.equals(job.getStatus()) ? "Archived"
                                    : job.getDeadline().isBefore(LocalDate.now()) ? "Closed" : "Open";
                        %>
                        <tr>
                            <td><%= job.getTitle() %></td>
                            <td><%= job.getModuleCode() %></td>
                            <td><%= applicantCount %></td>
                            <td><%= job.getDeadline() %></td>
                            <td><span class="badge <%= status.equals("Ongoing") ? "status-pending" : "status-accepted" %>"><%= status %></span></td>
                            <td class="text-end">
                                <a class="btn btn-sm btn-primary me-1" href="<%= request.getContextPath() %>/secure/mo/manage-job?action=view&jobId=<%= job.getId() %>">View</a>
                                <a class="btn btn-sm btn-secondary" href="<%= request.getContextPath() %>/secure/mo/application_list.jsp?jobId=<%= job.getId() %>">View Applications</a>
                            </td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </div>
</div>

<script>
    const jobFilterKeyword = document.getElementById('jobFilterKeyword');
    const jobFilterStatus = document.getElementById('jobFilterStatus');
    const jobFilterClear = document.getElementById('jobFilterClear');
    const jobListTable = document.getElementById('jobListTable');

    function applyJobFilters() {
        const keyword = jobFilterKeyword.value.trim().toLowerCase();
        const status = jobFilterStatus.value;

        const rows = jobListTable.querySelectorAll('tbody tr');
        rows.forEach(row => {
            const title = row.cells[0].textContent.toLowerCase();
            const module = row.cells[1].textContent.toLowerCase();
            const rowStatus = row.cells[4].textContent.trim();

            const matchKeyword = keyword === '' || title.includes(keyword) || module.includes(keyword);
            const matchStatus = status === '' || rowStatus === status;

            row.style.display = (matchKeyword && matchStatus) ? '' : 'none';
        });
    }

    jobFilterKeyword.addEventListener('input', applyJobFilters);
    jobFilterStatus.addEventListener('change', applyJobFilters);
    jobFilterClear.addEventListener('click', () => {
        jobFilterKeyword.value = '';
        jobFilterStatus.value = '';
        applyJobFilters();
    });
</script>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
