package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.mapper.ReportPermissionMapper;
import com.example.backend.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 权限服务实现
 *
 * 权限业务逻辑：
 * 1. ROLE_ADMIN (系统管理员): 拥有所有权限，无需配置
 * 2. ROLE_REPORT_MANAGER (报表管理员): 拥有所有报表的管理权限（编辑、查看、生成、下载）
 * 3. ROLE_REPORT_USER (报表用户): 拥有查看和生成报表权限
 * 4. 部门主管角色: 通过 report_permission 表配置特定模板的权限
 *    - ROLE_FINANCE_MANAGER (财务主管): 财务相关报表
 *    - ROLE_HR_MANAGER (人事主管): 人事相关报表
 *    - ROLE_SALES_MANAGER (销售主管): 销售相关报表
 *    - ROLE_WAREHOUSE_MANAGER (仓库主管): 仓库相关报表
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    /** 系统管理员角色编码 */
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    /** 报表管理员角色编码 */
    private static final String ROLE_REPORT_MANAGER = "ROLE_REPORT_MANAGER";

    /** 报表用户角色编码 */
    private static final String ROLE_REPORT_USER = "ROLE_REPORT_USER";

    @Autowired
    private ReportPermissionMapper reportPermissionMapper;

    @Override
    public boolean checkPermission(Long userId, Long templateId, Integer permissionType) {
        // 1. 系统管理员拥有所有权限
        if (isAdmin(userId)) {
            return true;
        }

        // 2. 报表管理员拥有所有报表的编辑权限（包含查看、生成、下载）
        if (isReportManager(userId)) {
            return true;
        }

        // 3. 报表用户拥有查看(1)和生成(2)权限
        if (isReportUser(userId) && permissionType <= 2) {
            return true;
        }

        // 4. 其他角色通过 report_permission 表检查权限
        return reportPermissionMapper.checkUserPermission(userId, templateId, permissionType) > 0;
    }

    @Override
    public boolean checkCurrentUserPermission(Long templateId, Integer permissionType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return checkPermission(userId, templateId, permissionType);
    }

    @Override
    public boolean isAdmin(Long userId) {
        return reportPermissionMapper.checkUserHasRole(userId, ROLE_ADMIN) > 0;
    }

    @Override
    public boolean isCurrentUserAdmin() {
        Long userId = StpUtil.getLoginIdAsLong();
        return isAdmin(userId);
    }

    @Override
    public boolean isReportManager(Long userId) {
        return reportPermissionMapper.checkUserHasRole(userId, ROLE_REPORT_MANAGER) > 0;
    }

    @Override
    public boolean isReportUser(Long userId) {
        return reportPermissionMapper.checkUserHasRole(userId, ROLE_REPORT_USER) > 0;
    }

    @Override
    public List<Long> getUserPermittedTemplateIds(Long userId, Integer permissionType) {
        // 管理员和报表管理员可以访问所有模板，返回null表示不限制
        if (isAdmin(userId) || isReportManager(userId)) {
            return null;
        }

        // 报表用户可以查看和生成所有已发布模板
        if (isReportUser(userId) && permissionType <= 2) {
            return null;
        }

        // 其他角色根据配置返回
        List<Long> templateIds = reportPermissionMapper.selectTemplateIdsByUserId(userId, permissionType);
        return templateIds != null ? templateIds : Collections.emptyList();
    }

    @Override
    public List<Long> getCurrentUserPermittedTemplateIds(Integer permissionType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return getUserPermittedTemplateIds(userId, permissionType);
    }

    @Override
    public List<String> getUserRoleCodes(Long userId) {
        return reportPermissionMapper.selectUserRoleCodes(userId);
    }
}
