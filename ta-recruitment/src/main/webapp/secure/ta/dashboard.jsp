<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>TA Dashboard</h2>
            <p class="text-muted">Welcome, <strong><%= currentUser.getEmail() %></strong>!</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
    </div>

    <div class="row mt-4">
        <div class="col-md-3">
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">My Profile</h5>
                    <p class="card-text">Fill in your information and upload your resume so MOs can review your background.</p>
                    <a href="<%= request.getContextPath() %>/secure/ta/profile" class="btn btn-sm btn-primary">Edit Profile</a>
                </div>
            </div>

            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">My Applications</h5>
                    <p class="card-text">View the status of your submitted applications.</p>
                    <a href="<%= request.getContextPath() %>/secure/ta/applications" class="btn btn-sm btn-primary">View Applications</a>
                </div>
            </div>

            <% if (currentUser.getRole() == User.Role.MO) { %>
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">MO Portal</h5>
                    <p class="card-text">Go to the Module Organizer (MO) dashboard to view applications and post positions.</p>
                    <a href="<%= request.getContextPath() %>/secure/mo/dashboard.jsp" class="btn btn-sm btn-warning">Go to MO Dashboard</a>
                </div>
            </div>
            <% } %>
            <% if (currentUser.getRole() == User.Role.ADMIN) { %>
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">Admin Portal</h5>
                    <p class="card-text">Go to the Admin console to view system metrics, user management, and audit logs.</p>
                    <a href="<%= request.getContextPath() %>/secure/admin/dashboard.jsp" class="btn btn-sm btn-danger">Go to Admin Console</a>
                </div>
            </div>
            <% } %>
        </div>

        <div class="col-md-9">
            <div class="card mb-4">
                <div class="card-body">
                    <h5 class="card-title">Positions</h5>
                    <p class="text-muted">(Demo mode - static example.)</p>

                    <div class="row g-2 mb-3">
                        <div class="col-md-4">
                            <input id="jobFilterKeyword" type="text" class="form-control" placeholder="Keyword (position/module)" />
                        </div>
                        <div class="col-md-3">
                            <select id="jobFilterStatus" class="form-select">
                                <option value="">All statuses</option>
                                <option value="Open">Open</option>
                                <option value="Closed">Closed</option>
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
                            <th>Deadline</th>
                            <th>Status</th>
                            <th class="text-end">Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>Network Course TA</td>
                            <td>CS301</td>
                            <td>2026-04-07</td>
                            <td><span class="badge status-pending">Open</span></td>
                            <td class="text-end"><a class="btn btn-sm btn-primary" href="#">Apply</a></td>
                        </tr>
                        <tr>
                            <td>Compilers Homework Tutor</td>
                            <td>CS402</td>
                            <td>2026-04-12</td>
                            <td><span class="badge status-pending">Open</span></td>
                            <td class="text-end"><a class="btn btn-sm btn-primary" href="#">Apply</a></td>
                        </tr>
                        <tr>
                            <td>English Writing Reviewer</td>
                            <td>EN201</td>
                            <td>2026-03-31</td>
                            <td><span class="badge status-rejected">Closed</span></td>
                            <td class="text-end"><button class="btn btn-sm btn-outline-secondary" disabled>Apply</button></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">Recent Activity</h5>
                    <ul class="list-group list-group-flush">
                        <li class="list-group-item">Your resume was uploaded successfully on 2026-03-15.</li>
                        <li class="list-group-item">You have 2 pending applications; responses are expected within 1 business day.</li>
                        <li class="list-group-item">There are 5 available TA positions you can apply for.</li>
                    </ul>
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
            const rowStatus = row.cells[3].textContent.trim();

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
