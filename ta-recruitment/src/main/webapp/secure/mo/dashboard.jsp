<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>MO 仪表盘</h2>
            <p class="text-muted">欢迎, <strong><%= currentUser.getEmail() %></strong>！这里是模块组织者的管理入口。</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/logout">退出登录</a>
        </div>
    </div>

    <div class="row mt-4">
        <div class="col-md-4">
            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">发布/管理职位</h5>
                    <p class="card-text">发布新的 TA 职位信息或编辑已有职位。</p>
                    <a href="<%= request.getContextPath() %>/secure/mo/post_position.jsp" class="btn btn-sm btn-primary">发布新职位</a>
                </div>
            </div>

            <div class="card mb-3">
                <div class="card-body">
                    <h5 class="card-title">查看申请</h5>
                    <p class="card-text">浏览各职位的申请人并更新筛选状态。</p>
                    <a href="<%= request.getContextPath() %>/secure/mo/application_list.jsp" class="btn btn-sm btn-primary">查看申请列表</a>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">招聘进度</h5>
                    <p class="card-text">查看各职位招募进度与已招聘人数。</p>
                    <a href="<%= request.getContextPath() %>/secure/mo/progress.jsp" class="btn btn-sm btn-primary">查看进度</a>
                </div>
            </div>
        </div>

        <div class="col-md-8">
            <div class="card mb-4">
                <div class="card-body">
                    <h5 class="card-title">最近发布的职位</h5>
                    <p class="text-muted">展示你最近发布的职位及当前申请情况。</p>

                    <div class="row g-2 mb-3">
                        <div class="col-md-4">
                            <input id="jobFilterKeyword" type="text" class="form-control" placeholder="关键字（职位/模块）" />
                        </div>
                        <div class="col-md-3">
                            <select id="jobFilterStatus" class="form-select">
                                <option value="">全部状态</option>
                                <option value="进行中">进行中</option>
                                <option value="已筛选">已筛选</option>
                                <option value="已完成">已完成</option>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <button id="jobFilterClear" class="btn btn-outline-secondary w-100">清除</button>
                        </div>
                    </div>

                    <table id="jobListTable" class="table table-hover">
                        <thead>
                        <tr>
                            <th>职位</th>
                            <th>模块</th>
                            <th>申请人数</th>
                            <th>截止</th>
                            <th>状态</th>
                            <th class="text-end">操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>数据结构助教</td>
                            <td>CS201</td>
                            <td>12</td>
                            <td>2026-04-10</td>
                            <td><span class="badge status-pending">进行中</span></td>
                            <td class="text-end"><a class="btn btn-sm btn-secondary" href="#">查看申请</a></td>
                        </tr>
                        <tr>
                            <td>操作系统辅导</td>
                            <td>CS303</td>
                            <td>6</td>
                            <td>2026-03-28</td>
                            <td><span class="badge status-shortlisted">已筛选</span></td>
                            <td class="text-end"><a class="btn btn-sm btn-secondary" href="#">查看申请</a></td>
                        </tr>
                        <tr>
                            <td>学术写作指导</td>
                            <td>EN302</td>
                            <td>3</td>
                            <td>2026-03-20</td>
                            <td><span class="badge status-accepted">已完成</span></td>
                            <td class="text-end"><a class="btn btn-sm btn-secondary" href="#">查看申请</a></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </div>
</div>

<script>
    const jobFilterKeyword = document.getElementById('jobFilterKeyword');
    const jobFilterStatus = document.getElementById('jobFilterStatus');
    const jobFilterClear = document.getElementById('jobFilterClear');
    const jobListTable = document.getElementById('jobListTable');

    function applyJobFilters() {
        const keyword = jobFilterKeyword.value.trim().toLowerCase();
        const status = jobFilterStatus.value;

        const rows = jobListTable.querySelectorAll('tbody tr');
        rows.forEach(row => {
            const title = row.cells[0].textContent.toLowerCase();
            const module = row.cells[1].textContent.toLowerCase();
            const rowStatus = row.cells[4].textContent.trim();

            const matchKeyword = keyword === '' || title.includes(keyword) || module.includes(keyword);
            const matchStatus = status === '' || rowStatus === status;

            row.style.display = (matchKeyword && matchStatus) ? '' : 'none';
        });
    }

    jobFilterKeyword.addEventListener('input', applyJobFilters);
    jobFilterStatus.addEventListener('change', applyJobFilters);
    jobFilterClear.addEventListener('click', () => {
        jobFilterKeyword.value = '';
        jobFilterStatus.value = '';
        applyJobFilters();
    });
</script>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
