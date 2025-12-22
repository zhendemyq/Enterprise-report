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
 * 权限类型：1-查看 2-生成 3-下载 4-编辑
 *
 * 权限业务逻辑：
 * 1. ADMIN (系统管理员): 拥有所有权限，无需配置
 * 2. REPORT_MANAGER (报表管理员): 管理模板和数据源，拥有所有报表的编辑权限（1,2,3,4）
 * 3. REPORT_USER (报表用户): 查看和生成报表（1,2）
 * 4. REPORT_VIEWER (报表查看员): 只能查看报表（1）
 * 5. 部门主管角色: 通过 report_permission 表配置特定模板的权限
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private ReportPermissionMapper reportPermissionMapper;

    /**
     * 检查用户是否有指定角色（兼容有无 ROLE_ 前缀）
     */
    private boolean hasRole(Long userId, String... roleCodes) {
        for (String roleCode : roleCodes) {
            if (reportPermissionMapper.checkUserHasRole(userId, roleCode) > 0) {
                return true;
            }
            // 兼容 ROLE_ 前缀
            if (!roleCode.startsWith("ROLE_")) {
                if (reportPermissionMapper.checkUserHasRole(userId, "ROLE_" + roleCode) > 0) {
                    return true;
                }
            } else {
                // 如果是 ROLE_XXX，也检查 XXX
                String shortCode = roleCode.substring(5);
                if (reportPermissionMapper.checkUserHasRole(userId, shortCode) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean checkPermission(Long userId, Long templateId, Integer permissionType) {
        // 1. 系统管理员拥有所有权限
        if (isAdmin(userId)) {
            return true;
        }

        // 2. 报表管理员拥有所有报表的编辑权限（包含查看、生成、下载、编辑）
        if (isReportManager(userId)) {
            return true;
        }

        // 3. 报表用户拥有查看(1)和生成(2)权限
        if (isReportUser(userId) && permissionType <= 2) {
            return true;
        }

        // 4. 报表查看员只能查看(1)
        if (isReportViewer(userId) && permissionType == 1) {
            return true;
        }

        // 5. 其他角色通过 report_permission 表检查权限
        return reportPermissionMapper.checkUserPermission(userId, templateId, permissionType) > 0;
    }

    @Override
    public boolean checkCurrentUserPermission(Long templateId, Integer permissionType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return checkPermission(userId, templateId, permissionType);
    }

    @Override
    public boolean isAdmin(Long userId) {
        return hasRole(userId, "ADMIN", "ROLE_ADMIN");
    }

    @Override
    public boolean isCurrentUserAdmin() {
        Long userId = StpUtil.getLoginIdAsLong();
        return isAdmin(userId);
    }

    @Override
    public boolean isReportManager(Long userId) {
        return hasRole(userId, "REPORT_MANAGER", "ROLE_REPORT_MANAGER");
    }

    @Override
    public boolean isReportUser(Long userId) {
        return hasRole(userId, "REPORT_USER", "ROLE_REPORT_USER");
    }

    /**
     * 检查用户是否是报表查看员（只读权限）
     */
    public boolean isReportViewer(Long userId) {
        return hasRole(userId, "REPORT_VIEWER", "ROLE_REPORT_VIEWER");
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

        // 报表查看员只能查看所有已发布模板
        if (isReportViewer(userId) && permissionType == 1) {
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
