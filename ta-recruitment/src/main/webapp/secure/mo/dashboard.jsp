<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
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
                    <h5 class="card-title">Post / Manage Positions</h5>
                    <p class="card-text">Create new TA positions or update existing postings.</p>
                    <a href="<%= request.getContextPath() %>/secure/mo/post_position.jsp" class="btn btn-sm btn-primary">Post New Position</a>
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
                                <option value="Ongoing">Ongoing</option>
                                <option value="Shortlisted">Shortlisted</option>
                                <option value="Completed">Completed</option>
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
                        <tr>
                            <td>Data Structures TA</td>
                            <td>CS201</td>
                            <td>12</td>
                            <td>2026-04-10</td>
                            <td><span class="badge status-pending">Ongoing</span></td>
                            <td class="text-end"><a class="btn btn-sm btn-secondary" href="#">View Applications</a></td>
                        </tr>
                        <tr>
                            <td>Operating Systems Tutor</td>
                            <td>CS303</td>
                            <td>6</td>
                            <td>2026-03-28</td>
                            <td><span class="badge status-shortlisted">Shortlisted</span></td>
                            <td class="text-end"><a class="btn btn-sm btn-secondary" href="#">View Applications</a></td>
                        </tr>
                        <tr>
                            <td>Academic Writing Mentor</td>
                            <td>EN302</td>
                            <td>3</td>
                            <td>2026-03-20</td>
                            <td><span class="badge status-accepted">Completed</span></td>
                            <td class="text-end"><a class="btn btn-sm btn-secondary" href="#">View Applications</a></td>
                        </tr>
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
