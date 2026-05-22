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

    <c:if test="${not empty redirectAfterProfile}">
        <div class="alert alert-warning">
            <strong>Please complete your profile (name, student ID, major, phone, and resume) before applying for positions.</strong>
        </div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert-success">${success}</div>
    </c:if>
    <c:if test="${not empty uploadError}">
        <div class="alert alert-danger"><strong>${uploadError}</strong></div>
    </c:if>
    <c:if test="${not empty uploadMessage}">
        <div class="alert alert-info"><strong>${uploadMessage}</strong></div>
    </c:if>
    <c:if test="${not empty uploadSuccess}">
        <div class="alert alert-success">
            <span class="fw-bold text-success">✓ Resume uploaded successfully.</span>
            <c:if test="${not empty profile.resumeFileName}">
                <a class="btn btn-sm btn-success ms-2" href="${pageContext.request.contextPath}/secure/ta/resume?file=${profile.resumeFileName}" target="_blank">Preview resume</a>
            </c:if>
        </div>
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
                        <label class="form-label">Student ID</label>
                        <input type="text" name="studentId" class="form-control" value="<%= profile.getStudentId() == null ? "" : profile.getStudentId() %>" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Major</label>
                        <input type="text" name="major" class="form-control" value="<%= profile.getMajor() == null ? "" : profile.getMajor() %>" required>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Phone</label>
                        <input type="text" name="phone" class="form-control" value="<%= profile.getPhone() == null ? "" : profile.getPhone() %>" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">GPA</label>
                        <input type="number" step="0.01" min="0" max="4.0" name="gpa" class="form-control" value="<%= profile.getGpa() == null ? "" : profile.getGpa() %>">
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12 mb-3">
                        <label class="form-label">Skills (comma-separated)</label>
                        <input type="text" name="skills" class="form-control" value="<%= profile.getSkills() == null ? "" : String.join(", ", profile.getSkills()) %>">
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Resume</label>
                    <div class="input-group">
                        <input type="text" id="resumeFileName" class="form-control" placeholder="No file selected" readonly>
                        <button type="button" class="btn btn-outline-secondary" id="chooseResumeBtn">Choose resume</button>
                    </div>
                    <input type="file" name="resume" id="resumeInput" accept="application/pdf" class="d-none">
                    <div id="resumeValidationText" class="form-text text-muted fw-bold">Supported format: <span class="text-primary">PDF</span>; size limit: <span class="text-primary">≤ 5MB</span>.</div>
                    <c:if test="${not empty profile.resumeFileName}">
                        <div class="mt-2">
                            Current resume: <strong>${profile.resumeFileName}</strong>
                            <a class="btn btn-sm btn-outline-secondary ms-2" href="${pageContext.request.contextPath}/secure/ta/resume?file=${profile.resumeFileName}" target="_blank">View</a>
                            <button type="submit" name="action" value="removeResume" class="btn btn-sm btn-outline-danger ms-2" onclick="return confirm('Remove the uploaded resume?');">Remove</button>
                        </div>
                    </c:if>
                </div>

                <button type="submit" class="btn btn-primary">Save Profile</button>

                <script>
                    (function() {
                        var fileInput = document.getElementById('resumeInput');
                        var fileNameField = document.getElementById('resumeFileName');
                        var chooseBtn = document.getElementById('chooseResumeBtn');
                        var validationText = document.getElementById('resumeValidationText');
                        var form = document.querySelector('form');
                        var maxSize = 5 * 1024 * 1024;
                        var allowedPattern = /\.pdf$/i;

                        function updateValidation(message, status) {
                            validationText.textContent = message;
                            validationText.classList.remove('text-success', 'text-danger', 'text-muted');
                            if (status === 'success') {
                                validationText.classList.add('text-success');
                            } else if (status === 'error') {
                                validationText.classList.add('text-danger');
                            } else {
                                validationText.classList.add('text-muted');
                            }
                        }

                        function validateResume(file) {
                            if (!file) {
                                return { valid: true, message: 'Supported formats: PDF, Word (.doc, .docx); size limit: ≤ 5MB.' };
                            }
                            if (!allowedPattern.test(file.name)) {
                                return { valid: false, message: 'Unsupported format. Please choose a PDF document.' };
                            }
                            if (file.size > maxSize) {
                                return { valid: false, message: 'File too large. Please upload a file smaller than 5MB.' };
                            }
                            return { valid: true, message: 'File is valid and ready to upload.' };
                        }

                        chooseBtn.addEventListener('click', function() {
                            fileInput.click();
                        });

                        fileInput.addEventListener('change', function() {
                            var file = fileInput.files[0];
                            fileNameField.value = file ? file.name : 'No file selected';
                            var validation = validateResume(file);
                            if (validation.valid) {
                                fileNameField.classList.remove('is-invalid');
                                fileNameField.classList.add('is-valid');
                                updateValidation(validation.message, 'success');
                            } else {
                                fileNameField.classList.remove('is-valid');
                                fileNameField.classList.add('is-invalid');
                                updateValidation(validation.message, 'error');
                            }
                        });

                        form.addEventListener('submit', function(event) {
                            var file = fileInput.files[0];
                            var validation = validateResume(file);
                            if (file && !validation.valid) {
                                event.preventDefault();
                                fileNameField.classList.add('is-invalid');
                                updateValidation('Please fix the resume upload issue before saving.', 'error');
                            }
                        });
                    })();
                </script>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
