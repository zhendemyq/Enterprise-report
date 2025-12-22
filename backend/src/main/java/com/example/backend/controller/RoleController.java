package com.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.dto.RoleDTO;
import com.example.backend.dto.RoleQueryDTO;
import com.example.backend.dto.TemplatePermissionDTO;
import com.example.backend.service.RoleService;
import com.example.backend.vo.RoleVO;
import com.example.backend.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理", description = "角色CRUD、权限配置等接口")
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Operation(summary = "获取所有角色列表")
    @GetMapping("/list")
    public Result<List<RoleVO>> listRoles() {
        return Result.success(roleService.listRoles());
    }

    @Operation(summary = "分页查询角色")
    @GetMapping("/page")
    public Result<PageResult<RoleVO>> pageRoles(RoleQueryDTO queryDTO) {
        IPage<RoleVO> page = roleService.pageRoles(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public Result<RoleVO> getRoleDetail(@PathVariable Long id) {
        return Result.success(roleService.getRoleDetail(id));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<Long> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        return Result.success(roleService.createRole(roleDTO));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
        roleService.updateRole(id, roleDTO);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @Operation(summary = "切换角色状态")
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        roleService.toggleStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "获取角色权限")
    @GetMapping("/{roleId}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        return Result.success(roleService.getRolePermissions(roleId));
    }

    @Operation(summary = "保存角色权限")
    @PutMapping("/{roleId}/permissions")
    public Result<Void> saveRolePermissions(@PathVariable Long roleId, @RequestBody List<Long> templateIds) {
        roleService.saveRolePermissions(roleId, templateIds);
        return Result.success();
    }

    @Operation(summary = "获取角色细粒度权限")
    @GetMapping("/{roleId}/detailed-permissions")
    public Result<List<TemplatePermissionDTO>> getRoleDetailedPermissions(@PathVariable Long roleId) {
        return Result.success(roleService.getRoleDetailedPermissions(roleId));
    }

    @Operation(summary = "保存角色细粒度权限")
    @PutMapping("/{roleId}/detailed-permissions")
    public Result<Void> saveRoleDetailedPermissions(@PathVariable Long roleId, @RequestBody List<TemplatePermissionDTO> permissions) {
        roleService.saveRoleDetailedPermissions(roleId, permissions);
        return Result.success();
    }

    @Operation(summary = "获取角色下的用户列表")
    @GetMapping("/{roleId}/users")
    public Result<List<UserVO>> getRoleUsers(@PathVariable Long roleId) {
        return Result.success(roleService.getRoleUsers(roleId));
    }
}
