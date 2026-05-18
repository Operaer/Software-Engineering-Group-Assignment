<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    String error = (String) request.getAttribute("error");
    String usernameValue = (String) request.getAttribute("username");
    String emailValue = (String) request.getAttribute("email");

    if (usernameValue == null) {
        usernameValue = "";
    }
    if (emailValue == null) {
        emailValue = "";
    }
%>

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-7">
            <div class="card shadow-sm">
                <div class="card-body">
                    <h1 class="h3 mb-3 text-center">Create a TA Account</h1>
                    <p class="text-muted text-center">
                        Public registration is available for Teaching Assistant applicants only.
                    </p>

                    <form method="post" action="<%= request.getContextPath() %>/register" novalidate>
                        <div class="mb-3">
                            <label for="username" class="form-label">Username</label>
                            <input
                                    type="text"
                                    class="form-control"
                                    id="username"
                                    name="username"
                                    placeholder="Choose a username"
                                    value="<%= usernameValue %>"
                                    required>
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <input
                                    type="email"
                                    class="form-control"
                                    id="email"
                                    name="email"
                                    placeholder="Enter your email"
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
                                    placeholder="Create a password"
                                    required>
                        </div>

                        <div class="mb-3">
                            <label for="confirmPassword" class="form-label">Confirm Password</label>
                            <input
                                    type="password"
                                    class="form-control"
                                    id="confirmPassword"
                                    name="confirmPassword"
                                    placeholder="Re-enter your password"
                                    required>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary">Create Account</button>
                        </div>
                    </form>

                    <% if (error != null && !error.isBlank()) { %>
                        <div class="alert alert-danger mt-3" role="alert">
                            <%= error %>
                        </div>
                    <% } %>

                    <p class="text-center mt-4 mb-0 text-muted small">
                        Already have an account?
                        <a href="<%= request.getContextPath() %>/" class="text-decoration-none">Sign in here</a>
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>