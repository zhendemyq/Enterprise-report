package com.example.backend.service;

import java.util.List;

/**
 * 权限服务接口
 *
 * 权限业务逻辑：
 * - ROLE_ADMIN: 系统管理员，拥有所有权限
 * - ROLE_REPORT_MANAGER: 报表管理员，管理报表模板和数据源
 * - ROLE_REPORT_USER: 报表用户，查看和生成报表
 * - 部门主管角色: 对应部门的报表权限
 */
public interface PermissionService {

    /**
     * 检查用户是否有指定模板的权限
     * @param userId 用户ID
     * @param templateId 模板ID
     * @param permissionType 权限类型 1-查看 2-生成 3-下载 4-编辑
     * @return 是否有权限
     */
    boolean checkPermission(Long userId, Long templateId, Integer permissionType);

    /**
     * 检查当前登录用户是否有指定模板的权限
     * @param templateId 模板ID
     * @param permissionType 权限类型 1-查看 2-生成 3-下载 4-编辑
     * @return 是否有权限
     */
    boolean checkCurrentUserPermission(Long templateId, Integer permissionType);

    /**
     * 检查用户是否是管理员
     * @param userId 用户ID
     * @return 是否是管理员
     */
    boolean isAdmin(Long userId);

    /**
     * 检查当前登录用户是否是管理员
     * @return 是否是管理员
     */
    boolean isCurrentUserAdmin();

    /**
     * 检查用户是否是报表管理员
     * @param userId 用户ID
     * @return 是否是报表管理员
     */
    boolean isReportManager(Long userId);

    /**
     * 检查用户是否是报表用户（有查看和生成权限）
     * @param userId 用户ID
     * @return 是否是报表用户
     */
    boolean isReportUser(Long userId);

    /**
     * 获取用户有权限的模板ID列表
     * @param userId 用户ID
     * @param permissionType 权限类型
     * @return 模板ID列表
     */
    List<Long> getUserPermittedTemplateIds(Long userId, Integer permissionType);

    /**
     * 获取当前登录用户有权限的模板ID列表
     * @param permissionType 权限类型
     * @return 模板ID列表
     */
    List<Long> getCurrentUserPermittedTemplateIds(Integer permissionType);

    /**
     * 获取用户的角色编码列表
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getUserRoleCodes(Long userId);
}
