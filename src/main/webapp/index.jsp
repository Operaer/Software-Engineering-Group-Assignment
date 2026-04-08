<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    String error = (String) request.getAttribute("error");
    String emailValue = (String) request.getAttribute("email");
    if (emailValue == null) {
        emailValue = "";
    }

    String registerSuccess = (String) session.getAttribute("registerSuccess");
    if (registerSuccess != null) {
        session.removeAttribute("registerSuccess");
    }
%>

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-7">
            <div class="card shadow-sm">
                <div class="card-body">
                    <h1 class="h3 mb-3 text-center">BUPT International School TA Recruitment</h1>
                    <p class="text-muted text-center">
                        Sign in with your email and password. Your role will be determined by your account.
                    </p>

                    <form method="post" action="<%= request.getContextPath() %>/login" novalidate>
                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <input
                                    type="email"
                                    class="form-control"
                                    id="email"
                                    name="email"
                                    placeholder="example@bupt.edu.cn"
                                    value="<%= emailValue %>"
                                    required>
                        </div>

                        <div class="mb-3">
                            <label for="password" class="form-label">Password</label>
                            <input
                                    type="password"
                                    class="form-control"
                                    id="password"
                                    name="password"
                                    placeholder="Enter your password"
                                    required>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary">Sign In</button>
                        </div>
                    </form>

                    <% if (error != null && !error.isBlank()) { %>
                        <div class="alert alert-danger mt-3" role="alert">
                            <%= error %>
                        </div>
                    <% } %>

                    <% if (registerSuccess != null && !registerSuccess.isBlank()) { %>
                        <div class="alert alert-success mt-3" role="alert">
                            <%= registerSuccess %>
                        </div>
                    <% } %>

                    <p class="text-center mt-4 mb-0 text-muted small">
                        Don't have an account?
                        <a href="<%= request.getContextPath() %>/register" class="text-decoration-none">Create one</a>
                    </p>

                    <div class="mt-4 text-muted small">
                        <p class="mb-2"><strong>Demo accounts for testing:</strong></p>
                        <ul class="mb-0">
                            <li>TA: <code>ta1@example.com</code> / <code>123456</code></li>
                            <li>MO: <code>mo1@example.com</code> / <code>123456</code></li>
                            <li>Admin: <code>admin@example.com</code> / <code>admin123</code></li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>