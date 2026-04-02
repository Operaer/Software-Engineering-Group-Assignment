<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-7">
            <div class="card shadow-sm">
                <div class="card-body">
                    <h1 class="h3 mb-3 text-center">BUPT International School TA Recruitment</h1>
                    <p class="text-muted">Please select your role and log in: TA / MO / Admin.</p>
                    <form method="post" action="login" novalidate>
                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <input type="email" class="form-control" id="email" name="email" placeholder="example@bupt.edu.cn" required>
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">Password</label>
                            <input type="password" class="form-control" id="password" name="password" placeholder="Enter your password" required>
                        </div>
                        <div class="mb-3">
                            <label for="role" class="form-label">Role</label>
                            <select id="role" name="role" class="form-select" required>
                                <option value="TA" selected>Teaching Assistant (TA)</option>
                                <option value="MO">Module Organizer (MO)</option>
                                <option value="ADMIN">Administrator</option>
                            </select>
                        </div>
                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary">Sign In</button>
                        </div>
                    </form>

                    <%
                        String error = (String) request.getAttribute("error");
                        if (error != null && !error.isEmpty()) {
                    %>
                        <div class="alert alert-danger mt-3" role="alert">
                            <%= error %>
                        </div>
                    <%
                        }
                    %>

                    <div class="mt-4 text-muted small">
                        <p>Note:</p>
                        <ul>
                            <li>This is a demo prototype with simulated login; any email/password combination will work.</li>
                            <li>After login, you will be redirected to the dashboard for your role.</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
