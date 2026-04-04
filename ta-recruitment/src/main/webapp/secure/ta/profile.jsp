<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.bupt.ta.model.TAProfile" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    TAProfile profile = (TAProfile) request.getAttribute("profile");
    Boolean readOnly = (Boolean) request.getAttribute("readOnly");
    boolean isReadOnly = readOnly != null && readOnly;
    if (profile == null) {
        profile = new TAProfile();
    }
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2><%= isReadOnly ? "View TA Profile" : "TA Profile" %></h2>
            <p class="text-muted"><%= isReadOnly ? "Viewing profile for MO review." : "Fill in your information and upload your resume for MO review." %></p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <c:if test="${not empty errors}">
        <div class="alert alert-danger">
            <ul>
                <c:forEach var="error" items="${errors}">
                    <li>${error}</li>
                </c:forEach>
            </ul>
        </div>
    </c:if>
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
            <% if (!isReadOnly) { %>
            <form method="post" enctype="multipart/form-data">
            <% } %>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Email</label>
                        <input type="email" class="form-control" value="<%= profile.getEmail() %>" disabled>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Full Name <%= !isReadOnly ? "*" : "" %></label>
                        <input type="text" name="name" id="name" class="form-control" value="<%= profile.getName() == null ? "" : profile.getName() %>" <%= isReadOnly ? "disabled" : "required" %>>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Student ID <%= !isReadOnly ? "*" : "" %></label>
                        <input type="text" name="studentId" id="studentId" class="form-control" value="<%= profile.getStudentId() == null ? "" : profile.getStudentId() %>" <%= isReadOnly ? "disabled" : "required" %>>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Major</label>
                        <input type="text" name="major" class="form-control" value="<%= profile.getMajor() == null ? "" : profile.getMajor() %>" <%= isReadOnly ? "disabled" : "" %>>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Phone <%= !isReadOnly ? "*" : "" %></label>
                        <input type="text" name="phone" id="phone" class="form-control" value="<%= profile.getPhone() == null ? "" : profile.getPhone() %>" <%= isReadOnly ? "disabled" : "required" %>>
                    </div>
                    <div class="col-md-12 mb-3">
                        <label class="form-label">Skills (comma-separated)</label>
                        <input type="text" name="skills" class="form-control" value="<%= profile.getSkills() == null ? "" : String.join(", ", profile.getSkills()) %>" <%= isReadOnly ? "disabled" : "" %>>
                    </div>
                </div>

                <% if (!isReadOnly) { %>
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

                <button type="submit" id="submitBtn" class="btn btn-primary">Save Profile</button>
                <% } %>

            <% if (!isReadOnly) { %>
            </form>
            <% } %>
        </div>
    </div>
</div>

<% if (!isReadOnly) { %>
<script>
(function() {
    var fileInput = document.getElementById('resumeInput');
    var fileNameField = document.getElementById('resumeFileName');
    var chooseBtn = document.getElementById('chooseResumeBtn');
    var submitBtn = document.getElementById('submitBtn');
    var nameField = document.getElementById('name');
    var studentIdField = document.getElementById('studentId');
    var phoneField = document.getElementById('phone');

    chooseBtn.addEventListener('click', function() {
        fileInput.click();
    });

    fileInput.addEventListener('change', function() {
        var file = fileInput.files[0];
        fileNameField.value = file ? file.name : 'No file selected';
    });

    // Real-time validation
    function validateField(field, regex, errorMsg) {
        var value = field.value.trim();
        var isValid = regex.test(value);
        field.classList.toggle('is-invalid', !isValid && value !== '');
        field.classList.toggle('is-valid', isValid);
        return isValid;
    }

    nameField.addEventListener('input', function() {
        validateField(nameField, /.+/, 'Name is required');
    });

    studentIdField.addEventListener('input', function() {
        validateField(studentIdField, /^\d{8,}$/, 'Student ID must be at least 8 digits');
    });

    phoneField.addEventListener('input', function() {
        validateField(phoneField, /^\d{10,11}$/, 'Phone must be 10-11 digits');
    });

    // Form submission validation
    document.querySelector('form').addEventListener('submit', function(e) {
        var nameValid = nameField.value.trim() !== '';
        var studentIdValid = /^\d{8,}$/.test(studentIdField.value.trim());
        var phoneValid = /^\d{10,11}$/.test(phoneField.value.trim());

        if (!nameValid || !studentIdValid || !phoneValid) {
            e.preventDefault();
            alert('Please correct the errors before submitting.');
        }
    });
})();
</script>
<% } %>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
