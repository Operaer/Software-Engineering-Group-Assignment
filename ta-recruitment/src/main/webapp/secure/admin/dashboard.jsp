<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>管理员仪表盘</h2>
            <p class="text-muted">欢迎, <strong><%= currentUser.getEmail() %></strong>！</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/logout">退出登录</a>
        </div>
    </div>

    <div class="row mt-4">
        <div class="col-md-4">
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">用户管理</h5>
                    <p class="card-text">查看/编辑所有用户账号、角色与活跃状态。</p>
                    <a href="<%= request.getContextPath() %>/secure/admin/user_management.jsp" class="btn btn-sm btn-primary">进入用户管理</a>
                </div>
            </div>

            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">系统配置</h5>
                    <p class="card-text">调整站点配置（演示），例如通知开关与并发限制。</p>
                    <a href="<%= request.getContextPath() %>/secure/admin/system_settings.jsp" class="btn btn-sm btn-primary">进入系统配置</a>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">审计日志</h5>
                    <p class="card-text">查看关键操作日志，了解系统变更历史。</p>
                    <a href="<%= request.getContextPath() %>/secure/admin/audit_logs.jsp" class="btn btn-sm btn-primary">查看审计日志</a>
                </div>
            </div>
        </div>

        <div class="col-md-8">
            <div class="card mb-4">
                <div class="card-body">
                    <h5 class="card-title">核心指标</h5>
                    <div class="row text-center">
                        <div class="col-4">
                            <div class="mb-1 text-muted">总岗位数</div>
                            <div class="h3">42</div>
                        </div>
                        <div class="col-4">
                            <div class="mb-1 text-muted">总申请数</div>
                            <div class="h3">231</div>
                        </div>
                        <div class="col-4">
                            <div class="mb-1 text-muted">已录用 TA</div>
                            <div class="h3">63</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">工作量预警</h5>
                    <p class="text-muted">系统会根据各 TA 工作时长自动识别负荷过高情况（示例数据）。</p>
                    <table class="table table-hover">
                        <thead>
                        <tr>
                            <th>TA</th>
                            <th>已分配小时</th>
                            <th>安全上限</th>
                            <th>状态</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>张三</td>
                            <td>22</td>
                            <td>20</td>
                            <td><span class="badge status-rejected">超负荷</span></td>
                        </tr>
                        <tr>
                            <td>李四</td>
                            <td>15</td>
                            <td>20</td>
                            <td><span class="badge status-accepted">正常</span></td>
                        </tr>
                        <tr>
                            <td>王五</td>
                            <td>19</td>
                            <td>20</td>
                            <td><span class="badge status-shortlisted">接近上限</span></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
