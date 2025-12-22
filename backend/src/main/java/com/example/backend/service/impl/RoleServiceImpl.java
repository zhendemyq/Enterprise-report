package com.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.RoleDTO;
import com.example.backend.dto.RoleQueryDTO;
import com.example.backend.dto.TemplatePermissionDTO;
import com.example.backend.entity.ReportPermission;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReportPermissionMapper;
import com.example.backend.mapper.RoleMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.service.RoleService;
import com.example.backend.vo.RoleVO;
import com.example.backend.vo.UserVO;
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

    @Autowired
    private UserMapper userMapper;

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
     * - ROLE_ADMIN / ADMIN: 系统管理员
     * - ROLE_USER / USER: 普通用户
     * - ROLE_REPORT_MANAGER / REPORT_MANAGER: 报表管理员
     * - ROLE_REPORT_USER / REPORT_USER: 报表用户
     */
    private boolean isSystemRole(String roleCode) {
        if (roleCode == null) return false;
        String code = roleCode.toUpperCase();
        return code.equals("ROLE_ADMIN") || code.equals("ADMIN")
            || code.equals("ROLE_USER") || code.equals("USER")
            || code.equals("ROLE_REPORT_MANAGER") || code.equals("REPORT_MANAGER")
            || code.equals("ROLE_REPORT_USER") || code.equals("REPORT_USER");
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

    @Override
    public List<TemplatePermissionDTO> getRoleDetailedPermissions(Long roleId) {
        LambdaQueryWrapper<ReportPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportPermission::getRoleId, roleId);
        List<ReportPermission> permissions = reportPermissionMapper.selectList(wrapper);

        // 按模板ID分组
        java.util.Map<Long, TemplatePermissionDTO> permMap = new java.util.HashMap<>();

        for (ReportPermission perm : permissions) {
            Long templateId = perm.getTemplateId();
            TemplatePermissionDTO dto = permMap.computeIfAbsent(templateId, id -> {
                TemplatePermissionDTO newDto = new TemplatePermissionDTO();
                newDto.setTemplateId(id);
                newDto.setView(false);
                newDto.setGenerate(false);
                newDto.setDownload(false);
                newDto.setEdit(false);
                return newDto;
            });

            // 根据权限类型设置对应标志
            switch (perm.getPermissionType()) {
                case 1 -> dto.setView(true);
                case 2 -> dto.setGenerate(true);
                case 3 -> dto.setDownload(true);
                case 4 -> dto.setEdit(true);
            }
        }

        return new java.util.ArrayList<>(permMap.values());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleDetailedPermissions(Long roleId, List<TemplatePermissionDTO> permissions) {
        // 先删除原有权限
        LambdaQueryWrapper<ReportPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportPermission::getRoleId, roleId);
        reportPermissionMapper.delete(wrapper);

        // 保存新的细粒度权限
        if (permissions != null && !permissions.isEmpty()) {
            for (TemplatePermissionDTO perm : permissions) {
                Long templateId = perm.getTemplateId();

                // 根据各个权限标志插入对应记录
                if (Boolean.TRUE.equals(perm.getView())) {
                    insertPermission(roleId, templateId, 1);
                }
                if (Boolean.TRUE.equals(perm.getGenerate())) {
                    insertPermission(roleId, templateId, 2);
                }
                if (Boolean.TRUE.equals(perm.getDownload())) {
                    insertPermission(roleId, templateId, 3);
                }
                if (Boolean.TRUE.equals(perm.getEdit())) {
                    insertPermission(roleId, templateId, 4);
                }
            }
        }
    }

    /**
     * 插入单条权限记录
     */
    private void insertPermission(Long roleId, Long templateId, Integer permissionType) {
        ReportPermission permission = new ReportPermission();
        permission.setRoleId(roleId);
        permission.setTemplateId(templateId);
        permission.setPermissionType(permissionType);
        reportPermissionMapper.insert(permission);
    }

    @Override
    public List<UserVO> getRoleUsers(Long roleId) {
        // 查询拥有该角色的用户ID列表
        LambdaQueryWrapper<UserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(UserRole::getRoleId, roleId);
        List<UserRole> userRoles = userRoleMapper.selectList(urWrapper);

        if (userRoles.isEmpty()) {
            return List.of();
        }

        // 获取用户ID列表
        List<Long> userIds = userRoles.stream()
                .map(UserRole::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 查询用户信息
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(User::getId, userIds)
                .orderByDesc(User::getCreateTime);
        List<User> users = userMapper.selectList(userWrapper);

        // 转换为VO
        return users.stream()
                .map(user -> BeanUtil.copyProperties(user, UserVO.class))
                .collect(Collectors.toList());
    }
}
