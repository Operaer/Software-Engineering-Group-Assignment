<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>发布新职位（演示页面）</h2>
            <p class="text-muted">MO 可以在这里填写职位信息并发布到系统中。</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/mo/dashboard.jsp">返回 MO 仪表盘</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <form>
                <div class="mb-3">
                    <label class="form-label">职位名称</label>
                    <input type="text" class="form-control" placeholder="如：计算机网络助教">
                </div>
                <div class="mb-3">
                    <label class="form-label">所属模块</label>
                    <input type="text" class="form-control" placeholder="如：CS301">
                </div>
                <div class="mb-3">
                    <label class="form-label">截止日期</label>
                    <input type="date" class="form-control">
                </div>
                <div class="mb-3">
                    <label class="form-label">职位描述</label>
                    <textarea class="form-control" rows="4" placeholder="简要说明职责和要求"></textarea>
                </div>
                <button type="button" class="btn btn-primary">发布职位（演示）</button>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
