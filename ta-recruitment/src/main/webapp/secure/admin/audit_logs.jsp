<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>审计日志（演示）</h2>
            <p class="text-muted">展示系统中关键事件的简单日志列表。</p>
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
                    <th>时间</th>
                    <th>操作用户</th>
                    <th>类型</th>
                    <th>描述</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>2026-03-17 09:12</td>
                    <td>admin@example.com</td>
                    <td>用户管理</td>
                    <td>新增 MO 账号 mo2</td>
                </tr>
                <tr>
                    <td>2026-03-16 18:25</td>
                    <td>admin@example.com</td>
                    <td>系统配置</td>
                    <td>修改邮件通知开关</td>
                </tr>
                <tr>
                    <td>2026-03-15 14:07</td>
                    <td>admin@example.com</td>
                    <td>审核日志</td>
                    <td>查看 TA 申请列表</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
