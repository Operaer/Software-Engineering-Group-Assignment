<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>TA Recruitment System</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.1/dist/css/bootstrap.min.css" crossorigin="anonymous" />
    <link rel="stylesheet" href="assets/css/style.css" />
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/">BUPT TA Recruitment</a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent"
                aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav ms-auto mb-2 mb-lg-0">

                <c:choose>
                    <c:when test="${not empty sessionScope.currentUser}">

                        <!-- Dashboard -->
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/dashboard">
                                Dashboard
                            </a>
                        </li>

                        <!-- ✅ 新增：Change Password -->
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/secure/account/change-password">
                                Change Password
                            </a>
                        </li>

                        <!-- 可选：显示当前用户 -->
                        <li class="nav-item">
                            <span class="nav-link text-white">
                                Hello, ${sessionScope.currentUser.username}
                            </span>
                        </li>

                        <!-- Logout -->
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/logout">
                                Logout
                            </a>
                        </li>

                    </c:when>

                    <c:otherwise>

                        <!-- Login -->
                        <li class="nav-item">
                            <a class="nav-link fw-semibold text-warning"
                            href="${pageContext.request.contextPath}/">
                                Login
                            </a>
                        </li>

                        <!-- Register（统一风格） -->
                        <li class="nav-item">
                            <a class="nav-link fw-semibold text-warning"
                            href="${pageContext.request.contextPath}/register">
                                Register
                            </a>
                        </li>

                    </c:otherwise>
                </c:choose>

            </ul>
        </div>
    </div>
</nav>