<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>系统配置（演示）</h2>
            <p class="text-muted">这里可以调整应用内的演示配置（实际生产环境可连接配置中心或数据库）。</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/admin/dashboard.jsp">返回管理员仪表盘</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <form>
                <div class="mb-3">
                    <label class="form-label">站点名称</label>
                    <input type="text" class="form-control" value="BUPT TA 招募系统">
                </div>
                <div class="mb-3">
                    <label class="form-label">最大同时登录人数</label>
                    <input type="number" class="form-control" value="100">
                </div>
                <div class="mb-3">
                    <label class="form-label">邮件通知开关</label>
                    <select class="form-select">
                        <option selected>开启</option>
                        <option>关闭</option>
                    </select>
                </div>
                <button type="button" class="btn btn-primary">保存设置（演示）</button>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
