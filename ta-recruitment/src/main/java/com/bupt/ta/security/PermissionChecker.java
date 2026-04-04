package com.bupt.ta.security;

import com.bupt.ta.model.User;

public class PermissionChecker {

    // 检查用户是否有特定权限
    public static boolean hasPermission(User user, User.Role requiredRole) {
        if (user == null || !user.isActive()) {
            return false;
        }
        return user.hasPermission(requiredRole);
    }

    // 检查用户是否可以访问特定资源
    public static boolean canAccessResource(User user, String resource) {
        if (user == null || !user.isActive()) {
            return false;
        }

        switch (user.getRole()) {
            case TA:
                return resource.startsWith("/secure/ta/") || resource.equals("/secure/dashboard");
            case MO:
                return resource.startsWith("/secure/ta/") || resource.startsWith("/secure/mo/") || resource.equals("/secure/dashboard");
            case ADMIN:
                return resource.startsWith("/secure/"); // 管理员可以访问所有secure资源
            default:
                return false;
        }
    }

    // 特定操作权限检查
    public static boolean canModifyApplicationStatus(User user) {
        return hasPermission(user, User.Role.MO); // 只有MO及以上可以修改申请状态
    }

    public static boolean canPostPosition(User user) {
        return hasPermission(user, User.Role.MO); // 只有MO及以上可以发布职位
    }

    public static boolean canManageUsers(User user) {
        return hasPermission(user, User.Role.ADMIN); // 只有管理员可以管理用户
    }

    public static boolean canViewAuditLogs(User user) {
        return hasPermission(user, User.Role.ADMIN); // 只有管理员可以查看审计日志
    }
}