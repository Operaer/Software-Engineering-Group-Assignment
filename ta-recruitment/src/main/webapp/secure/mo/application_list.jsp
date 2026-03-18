<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Application List (Demo)</h2>
            <p class="text-muted">MO can review applications for all positions and filter results here.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/mo/dashboard.jsp">Back to MO Dashboard</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <div class="row g-2 mb-3">
                <div class="col-md-4">
                    <input id="appFilterKeyword" type="text" class="form-control" placeholder="Keyword (TA/position)" />
                </div>
                <div class="col-md-3">
                    <select id="appFilterStatus" class="form-select">
                        <option value="">All statuses</option>
                        <option value="Pending">Pending</option>
                        <option value="Shortlisted">Shortlisted</option>
                        <option value="Accepted">Accepted</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <button id="appFilterClear" class="btn btn-outline-secondary w-100">Clear</button>
                </div>
            </div>

            <table id="appListTable" class="table table-hover">
                <thead>
                <tr>
                    <th>TA</th>
                    <th>Position</th>
                    <th>Applied At</th>
                    <th>Status</th>
                    <th class="text-end">Action</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>ta1@example.com</td>
                    <td>Computer Networks TA</td>
                    <td>2026-03-15 14:32</td>
                    <td><span class="badge status-pending">Pending</span></td>
                    <td class="text-end"><button class="btn btn-sm btn-primary">Approve</button> <button class="btn btn-sm btn-outline-secondary">Reject</button></td>
                </tr>
                <tr>
                    <td>ta2@example.com</td>
                    <td>Compilers Homework Tutor</td>
                    <td>2026-03-14 09:18</td>
                    <td><span class="badge status-shortlisted">Shortlisted</span></td>
                    <td class="text-end"><button class="btn btn-sm btn-outline-secondary">View Resume</button></td>
                </tr>
                <tr>
                    <td>ta3@example.com</td>
                    <td>English Writing Reviewer</td>
                    <td>2026-03-10 11:05</td>
                    <td><span class="badge status-accepted">Accepted</span></td>
                    <td class="text-end"><button class="btn btn-sm btn-outline-secondary">View Resume</button></td>
                </tr>
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
            const ta = row.cells[0].textContent.toLowerCase();
            const position = row.cells[1].textContent.toLowerCase();
            const rowStatus = row.cells[3].textContent.trim();

            const matchKeyword = keyword === '' || ta.includes(keyword) || position.includes(keyword);
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
