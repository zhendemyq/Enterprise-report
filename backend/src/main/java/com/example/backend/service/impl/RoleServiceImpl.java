package com.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.RoleDTO;
import com.example.backend.dto.RoleQueryDTO;
import com.example.backend.entity.ReportPermission;
import com.example.backend.entity.Role;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReportPermissionMapper;
import com.example.backend.mapper.RoleMapper;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.service.RoleService;
import com.example.backend.vo.RoleVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private ReportPermissionMapper reportPermissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleDTO roleDTO) {
        // 检查角色编码是否存在
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleDTO.getRoleCode());
        if (count(wrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }

        Role role = BeanUtil.copyProperties(roleDTO, Role.class);
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        if (role.getSort() == null) {
            role.setSort(0);
        }
        save(role);
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long id, RoleDTO roleDTO) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }

        // 检查角色编码是否被其他角色使用
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleDTO.getRoleCode())
                .ne(Role::getId, id);
        if (count(wrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }

        BeanUtil.copyProperties(roleDTO, role, "id");
        updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }

        // 检查是否为系统角色
        if (isSystemRole(role.getRoleCode())) {
            throw new BusinessException("系统角色不允许删除");
        }

        // 检查是否有用户使用该角色
        Integer userCount = baseMapper.countUsersByRoleId(id);
        if (userCount != null && userCount > 0) {
            throw new BusinessException("该角色下有 " + userCount + " 位用户，请先移除用户后再删除");
        }

        // 删除角色权限关联
        LambdaQueryWrapper<ReportPermission> permWrapper = new LambdaQueryWrapper<>();
        permWrapper.eq(ReportPermission::getRoleId, id);
        reportPermissionMapper.delete(permWrapper);

        // 删除用户角色关联（物理删除）
        userRoleMapper.deleteByRoleIdPhysically(id);

        // 删除角色
        removeById(id);
    }

    @Override
    public RoleVO getRoleDetail(Long id) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        return BeanUtil.copyProperties(role, RoleVO.class);
    }

    @Override
    public List<RoleVO> listRoles() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Role::getSort)
                .orderByAsc(Role::getId);
        List<Role> roles = list(wrapper);
        return roles.stream()
                .map(role -> {
                    RoleVO roleVO = BeanUtil.copyProperties(role, RoleVO.class);
                    // 统计用户数量
                    roleVO.setUserCount(baseMapper.countUsersByRoleId(role.getId()));
                    // 判断是否为系统角色（核心角色不允许删除）
                    roleVO.setIsSystem(isSystemRole(role.getRoleCode()));
                    return roleVO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 判断是否为系统核心角色
     * 系统角色包括：
     * - ROLE_ADMIN: 系统管理员
     * - ROLE_USER: 普通用户
     * - ROLE_REPORT_MANAGER: 报表管理员
     * - ROLE_REPORT_USER: 报表用户
     */
    private boolean isSystemRole(String roleCode) {
        return "ROLE_ADMIN".equals(roleCode)
            || "ROLE_USER".equals(roleCode)
            || "ROLE_REPORT_MANAGER".equals(roleCode)
            || "ROLE_REPORT_USER".equals(roleCode);
    }

    @Override
    public IPage<RoleVO> pageRoles(RoleQueryDTO queryDTO) {
        Page<Role> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(queryDTO.getRoleCode()), Role::getRoleCode, queryDTO.getRoleCode())
                .like(StringUtils.isNotBlank(queryDTO.getRoleName()), Role::getRoleName, queryDTO.getRoleName())
                .eq(queryDTO.getStatus() != null, Role::getStatus, queryDTO.getStatus())
                .orderByAsc(Role::getSort)
                .orderByDesc(Role::getCreateTime);

        IPage<Role> rolePage = page(page, wrapper);
        return rolePage.convert(role -> BeanUtil.copyProperties(role, RoleVO.class));
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        role.setStatus(status);
        updateById(role);
    }

    @Override
    public List<Long> getRolePermissions(Long roleId) {
        LambdaQueryWrapper<ReportPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportPermission::getRoleId, roleId);
        List<ReportPermission> permissions = reportPermissionMapper.selectList(wrapper);
        return permissions.stream()
                .map(ReportPermission::getTemplateId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRolePermissions(Long roleId, List<Long> templateIds) {
        // 先删除原有权限
        LambdaQueryWrapper<ReportPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportPermission::getRoleId, roleId);
        reportPermissionMapper.delete(wrapper);

        // 保存新权限
        if (templateIds != null && !templateIds.isEmpty()) {
            for (Long templateId : templateIds) {
                // 为每个模板添加所有权限类型（查看、生成、下载、编辑）
                for (int permType = 1; permType <= 4; permType++) {
                    ReportPermission permission = new ReportPermission();
                    permission.setRoleId(roleId);
                    permission.setTemplateId(templateId);
                    permission.setPermissionType(permType);
                    reportPermissionMapper.insert(permission);
                }
            }
        }
    }
}
