<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>招聘进度（演示页面）</h2>
            <p class="text-muted">在这里可以看到当前所有职位的招聘进展状态。</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/mo/dashboard.jsp">返回 MO 仪表盘</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <div class="row">
                <div class="col-md-4 mb-3">
                    <div class="card">
                        <div class="card-body">
                            <h6>总职位数</h6>
                            <p class="display-6">8</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 mb-3">
                    <div class="card">
                        <div class="card-body">
                            <h6>已完成招聘</h6>
                            <p class="display-6">3</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 mb-3">
                    <div class="card">
                        <div class="card-body">
                            <h6>进行中</h6>
                            <p class="display-6">5</p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="mt-4">
                <h6>最近更新</h6>
                <ul class="list-group">
                    <li class="list-group-item">2026-03-15：新增“数据结构助教”职位。</li>
                    <li class="list-group-item">2026-03-14：更新“操作系统辅导”申请状态为“已筛选”。</li>
                    <li class="list-group-item">2026-03-10：标记“英语写作批改”为已完成招聘。</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
