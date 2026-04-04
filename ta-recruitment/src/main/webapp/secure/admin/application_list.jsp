<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User, com.bupt.ta.model.Application, java.util.List" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>Application Management</h2>
            <p class="text-muted">Admin can review and manage all applications.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <%
        if (success != null && !success.isEmpty()) {
    %>
        <div class="alert alert-success mt-3" role="alert">
            <%= success %>
        </div>
    <%
        }
        if (error != null && !error.isEmpty()) {
    %>
        <div class="alert alert-danger mt-3" role="alert">
            <%= error %>
        </div>
    <%
        }
    %>

    <div class="card mt-4">
        <div class="card-body">
            <table class="table table-hover">
                <thead>
                <tr>
                    <th>TA Email</th>
                    <th>Position</th>
                    <th>Applied At</th>
                    <th>Status</th>
                    <th class="text-end">Action</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (applications != null) {
                        for (Application app : applications) {
                %>
                    <tr>
                        <td><%= app.getTaEmail() %></td>
                        <td><%= app.getPositionTitle() %></td>
                        <td><%= app.getAppliedAt() %></td>
                        <td>
                            <span class="badge <%= app.getStatus() == Application.Status.Pending ? "bg-warning" : app.getStatus() == Application.Status.Shortlisted ? "bg-info" : "bg-success" %>">
                                <%= app.getStatus() %>
                            </span>
                        </td>
                        <td class="text-end">
                            <%
                                if (app.getStatus() == Application.Status.Pending) {
                            %>
                                <form method="post" style="display: inline;">
                                    <input type="hidden" name="applicationId" value="<%= app.getId() %>" />
                                    <input type="hidden" name="status" value="Shortlisted" />
                                    <button type="submit" class="btn btn-sm btn-primary">Shortlist</button>
                                </form>
                                <form method="post" style="display: inline;">
                                    <input type="hidden" name="applicationId" value="<%= app.getId() %>" />
                                    <input type="hidden" name="status" value="Accepted" />
                                    <button type="submit" class="btn btn-sm btn-success">Accept</button>
                                </form>
                            <%
                                } else if (app.getStatus() == Application.Status.Shortlisted) {
                            %>
                                <form method="post" style="display: inline;">
                                    <input type="hidden" name="applicationId" value="<%= app.getId() %>" />
                                    <input type="hidden" name="status" value="Accepted" />
                                    <button type="submit" class="btn btn-sm btn-success">Accept</button>
                                </form>
                            <%
                                }
                            %>
                        </td>
                    </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>