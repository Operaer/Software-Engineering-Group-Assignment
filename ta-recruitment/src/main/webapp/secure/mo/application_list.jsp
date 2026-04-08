<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.model.Application" %>
<%@ page import="com.bupt.ta.model.TAProfile" %>
<%@ page import="com.bupt.ta.storage.ProfileStorage" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Load TA profiles for resume information
    ProfileStorage profileStorage = new ProfileStorage(getServletContext());
    Map<String, TAProfile> profileCache = new HashMap<>();
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Application List</h2>
            <p class="text-muted">MO can review applications for all positions and filter results here.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
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
                    <th>TA Email</th>
                    <th>Name</th>
                    <th>Major</th>
                    <th>Position</th>
                    <th>Applied At</th>
                    <th>Skills</th>
                    <th>Status</th>
                    <th class="text-end">Action</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (applications != null && !applications.isEmpty()) {
                        for (Application app : applications) {
                            // Get TA profile information
                            TAProfile profile = profileCache.get(app.getTaEmail());
                            if (profile == null) {
                                profile = profileStorage.load(app.getTaEmail());
                                if (profile != null) {
                                    profileCache.put(app.getTaEmail(), profile);
                                }
                            }
                %>
                <tr>
                    <td><%= app.getTaEmail() %></td>
                    <td><%= profile != null && profile.getName() != null ? profile.getName() : "<span class=\"text-muted\">N/A</span>" %></td>
                    <td><%= profile != null && profile.getMajor() != null ? profile.getMajor() : "<span class=\"text-muted\">N/A</span>" %></td>
                    <td><%= app.getPositionTitle() %></td>
                    <td><%= app.getAppliedAt().atZone(java.time.ZoneId.systemDefault()).format(formatter) %></td>
                    <td>
                        <% if (profile != null && profile.getSkills() != null && !profile.getSkills().isEmpty()) { %>
                            <div style="max-width: 300px;">
                                <% for (String skill : profile.getSkills()) { %>
                                    <span class="badge bg-info me-1 mb-1"><%= skill %></span>
                                <% } %>
                            </div>
                        <% } else { %>
                            <span class="text-muted">No skills provided</span>
                        <% } %>
                    </td>
                    <td><span class="badge status-<%= app.getStatus().toLowerCase() %>"><%= app.getStatus() %></span></td>
                    <td class="text-end">
                        <% if (profile != null && profile.getResumeFileName() != null && !profile.getResumeFileName().isEmpty()) { %>
                            <a href="<%= request.getContextPath() %>/secure/resume-preview?file=<%= profile.getResumeFileName() %>&ta=<%= java.net.URLEncoder.encode(app.getTaEmail(), "UTF-8") %>" 
                               target="_blank" class="btn btn-sm btn-info">Preview Resume</a>
                        <% } %>
                        <% if ("Pending".equals(app.getStatus())) { %>
                        <form method="post" style="display: inline;">
                            <input type="hidden" name="applicationId" value="<%= app.getId() %>" />
                            <input type="hidden" name="status" value="Shortlisted" />
                            <button type="submit" class="btn btn-sm btn-primary">Shortlist</button>
                        </form>
                        <form method="post" style="display: inline;">
                            <input type="hidden" name="applicationId" value="<%= app.getId() %>" />
                            <input type="hidden" name="status" value="Rejected" />
                            <button type="submit" class="btn btn-sm btn-outline-secondary">Reject</button>
                        </form>
                        <% } else if ("Shortlisted".equals(app.getStatus())) { %>
                        <form method="post" style="display: inline;">
                            <input type="hidden" name="applicationId" value="<%= app.getId() %>" />
                            <input type="hidden" name="status" value="Accepted" />
                            <button type="submit" class="btn btn-sm btn-success">Accept</button>
                        </form>
                        <form method="post" style="display: inline;">
                            <input type="hidden" name="applicationId" value="<%= app.getId() %>" />
                            <input type="hidden" name="status" value="Rejected" />
                            <button type="submit" class="btn btn-sm btn-outline-secondary">Reject</button>
                        </form>
                        <% } else { %>
                        <span class="text-muted">No action available</span>
                        <% } %>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>8
                <tr>
                    <td colspan="6" class="text-center text-muted">No applications found.</td>
                </tr>
                <%
                    }
                %>
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
            if (row.cells.length < 7) return; // Skip the "no applications" row
            
            const ta = row.cells[0].textContent.toLowerCase();
            const name = row.cells[1].textContent.toLowerCase();
            const major = row.cells[2].textContent.toLowerCase();
            const position = row.cells[3].textContent.toLowerCase();
            const skills = row.cells[5].textContent.toLowerCase();
            const rowStatus = row.cells[6].textContent.trim();

            const matchKeyword = keyword === '' || ta.includes(keyword) || name.includes(keyword) || major.includes(keyword) || position.includes(keyword) || skills.includes(keyword);
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
