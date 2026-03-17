<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>用户管理（演示）</h2>
            <p class="text-muted">查看并管理系统中的用户账号与角色。</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/admin/dashboard.jsp">返回管理员仪表盘</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <table class="table table-hover">
                <thead>
                <tr>
                    <th>用户名</th>
                    <th>邮箱</th>
                    <th>角色</th>
                    <th>状态</th>
                    <th class="text-end">操作</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>ta1</td>
                    <td>ta1@example.com</td>
                    <td>TA</td>
                    <td><span class="badge status-accepted">活跃</span></td>
                    <td class="text-end"><button class="btn btn-sm btn-secondary">编辑</button></td>
                </tr>
                <tr>
                    <td>mo1</td>
                    <td>mo1@example.com</td>
                    <td>MO</td>
                    <td><span class="badge status-pending">待审核</span></td>
                    <td class="text-end"><button class="btn btn-sm btn-secondary">审核</button></td>
                </tr>
                <tr>
                    <td>admin</td>
                    <td>admin@example.com</td>
                    <td>ADMIN</td>
                    <td><span class="badge status-accepted">活跃</span></td>
                    <td class="text-end"><button class="btn btn-sm btn-secondary">查看</button></td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
