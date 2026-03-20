<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.bupt.ta.model.TAProfile" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    TAProfile profile = (TAProfile) request.getAttribute("profile");
    if (profile == null) {
        profile = new TAProfile();
    }
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>TA Profile</h2>
            <p class="text-muted">Fill in your information and upload your resume for MO review.</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <c:if test="${not empty success}">
        <div class="alert alert-success">${success}</div>
    </c:if>
    <c:if test="${not empty uploadError}">
        <div class="alert alert-danger">${uploadError}</div>
    </c:if>
    <c:if test="${not empty uploadMessage}">
        <div class="alert alert-info">${uploadMessage}</div>
    </c:if>

    <div class="card">
        <div class="card-body">
            <form method="post" enctype="multipart/form-data">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Email</label>
                        <input type="email" class="form-control" value="<%= profile.getEmail() %>" disabled>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Full Name</label>
                        <input type="text" name="name" class="form-control" value="<%= profile.getName() == null ? "" : profile.getName() %>" required>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Major</label>
                        <input type="text" name="major" class="form-control" value="<%= profile.getMajor() == null ? "" : profile.getMajor() %>" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Phone</label>
                        <input type="text" name="phone" class="form-control" value="<%= profile.getPhone() == null ? "" : profile.getPhone() %>">
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12 mb-3">
                        <label class="form-label">Skills (comma-separated)</label>
                        <input type="text" name="skills" class="form-control" value="<%= profile.getSkills() == null ? "" : String.join(", ", profile.getSkills()) %>">
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Resume (PDF, max 5MB)</label>
                    <div class="input-group">
                        <input type="text" id="resumeFileName" class="form-control" placeholder="No file selected" readonly>
                        <button type="button" class="btn btn-outline-secondary" id="chooseResumeBtn">Choose file</button>
                    </div>
                    <input type="file" name="resume" id="resumeInput" accept="application/pdf" class="d-none">
                    <c:if test="${not empty profile.resumeFileName}">
                        <div class="mt-2">
                            Current resume: <strong>${profile.resumeFileName}</strong>
                            <a class="btn btn-sm btn-outline-secondary ms-2" href="${pageContext.request.contextPath}/secure/ta/resume?file=${profile.resumeFileName}" target="_blank">View</a>
                        </div>
                    </c:if>
                </div>

                <button type="submit" class="btn btn-primary">Save Profile</button>

                <script>
                    (function() {
                        var fileInput = document.getElementById('resumeInput');
                        var fileNameField = document.getElementById('resumeFileName');
                        var chooseBtn = document.getElementById('chooseResumeBtn');

                        chooseBtn.addEventListener('click', function() {
                            fileInput.click();
                        });

                        fileInput.addEventListener('change', function() {
                            var file = fileInput.files[0];
                            fileNameField.value = file ? file.name : 'No file selected';
                        });
                    })();
                </script>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
