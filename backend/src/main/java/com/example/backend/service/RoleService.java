package com.example.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.RoleDTO;
import com.example.backend.dto.RoleQueryDTO;
import com.example.backend.dto.TemplatePermissionDTO;
import com.example.backend.entity.Role;
import com.example.backend.vo.RoleVO;
import com.example.backend.vo.UserVO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {

    /**
     * 创建角色
     */
    Long createRole(RoleDTO roleDTO);

    /**
     * 更新角色
     */
    void updateRole(Long id, RoleDTO roleDTO);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 获取角色详情
     */
    RoleVO getRoleDetail(Long id);

    /**
     * 获取所有角色列表
     */
    List<RoleVO> listRoles();

    /**
     * 分页查询角色
     */
    IPage<RoleVO> pageRoles(RoleQueryDTO queryDTO);

    /**
     * 切换角色状态
     */
    void toggleStatus(Long id, Integer status);

    /**
     * 获取角色权限
     */
    List<Long> getRolePermissions(Long roleId);

    /**
     * 获取角色细粒度权限
     */
    List<TemplatePermissionDTO> getRoleDetailedPermissions(Long roleId);

    /**
     * 保存角色权限
     */
    void saveRolePermissions(Long roleId, List<Long> templateIds);

    /**
     * 保存角色细粒度权限
     */
    void saveRoleDetailedPermissions(Long roleId, List<TemplatePermissionDTO> permissions);

    /**
     * 获取角色下的用户列表
     */
    List<UserVO> getRoleUsers(Long roleId);
}
