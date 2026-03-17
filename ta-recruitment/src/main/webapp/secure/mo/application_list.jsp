<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<%
    User currentUser = (User) session.getAttribute("currentUser");
%>

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <h2>申请列表（演示页面）</h2>
            <p class="text-muted">MO 可在这里查看所有职位的申请情况并进行筛选。</p>
        </div>
        <div>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/secure/mo/dashboard.jsp">返回 MO 仪表盘</a>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-body">
            <div class="row g-2 mb-3">
                <div class="col-md-4">
                    <input id="appFilterKeyword" type="text" class="form-control" placeholder="关键字（TA/职位）" />
                </div>
                <div class="col-md-3">
                    <select id="appFilterStatus" class="form-select">
                        <option value="">全部状态</option>
                        <option value="待审核">待审核</option>
                        <option value="已筛选">已筛选</option>
                        <option value="已录用">已录用</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <button id="appFilterClear" class="btn btn-outline-secondary w-100">清除</button>
                </div>
            </div>

            <table id="appListTable" class="table table-hover">
                <thead>
                <tr>
                    <th>TA</th>
                    <th>职位</th>
                    <th>申请时间</th>
                    <th>状态</th>
                    <th class="text-end">操作</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>ta1@example.com</td>
                    <td>计算机网络助教</td>
                    <td>2026-03-15 14:32</td>
                    <td><span class="badge status-pending">待审核</span></td>
                    <td class="text-end"><button class="btn btn-sm btn-primary">通过</button> <button class="btn btn-sm btn-outline-secondary">拒绝</button></td>
                </tr>
                <tr>
                    <td>ta2@example.com</td>
                    <td>编译原理作业辅导</td>
                    <td>2026-03-14 09:18</td>
                    <td><span class="badge status-shortlisted">已筛选</span></td>
                    <td class="text-end"><button class="btn btn-sm btn-outline-secondary">查看简历</button></td>
                </tr>
                <tr>
                    <td>ta3@example.com</td>
                    <td>英语写作批改</td>
                    <td>2026-03-10 11:05</td>
                    <td><span class="badge status-accepted">已录用</span></td>
                    <td class="text-end"><button class="btn btn-sm btn-outline-secondary">查看简历</button></td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script>
    const appFilterKeyword = document.getElementById('appFilterKeyword');
    const appFilterStatus = document.getElementById('appFilterStatus');
    const appFilterClear = document.getElementById('appFilterClear');
    const appListTable = document.getElementById('appListTable');

    function applyAppFilters() {
        const keyword = appFilterKeyword.value.trim().toLowerCase();
        const status = appFilterStatus.value;

        const rows = appListTable.querySelectorAll('tbody tr');
        rows.forEach(row => {
            const ta = row.cells[0].textContent.toLowerCase();
            const position = row.cells[1].textContent.toLowerCase();
            const rowStatus = row.cells[3].textContent.trim();

            const matchKeyword = keyword === '' || ta.includes(keyword) || position.includes(keyword);
            const matchStatus = status === '' || rowStatus === status;

            row.style.display = (matchKeyword && matchStatus) ? '' : 'none';
        });
    }

    appFilterKeyword.addEventListener('input', applyAppFilters);
    appFilterStatus.addEventListener('change', applyAppFilters);
    appFilterClear.addEventListener('click', () => {
        appFilterKeyword.value = '';
        appFilterStatus.value = '';
        applyAppFilters();
    });
</script>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
