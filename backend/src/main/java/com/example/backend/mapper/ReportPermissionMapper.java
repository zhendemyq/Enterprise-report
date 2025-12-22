package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ReportPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 报表权限Mapper
 */
@Mapper
public interface ReportPermissionMapper extends BaseMapper<ReportPermission> {

    /**
     * 查询用户有权限的模板ID列表
     */
    @Select("SELECT DISTINCT rp.template_id FROM report_permission rp " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND rp.permission_type >= #{permissionType}")
    List<Long> selectTemplateIdsByUserId(@Param("userId") Long userId, @Param("permissionType") Integer permissionType);

    /**
     * 检查用户是否有指定模板的权限
     */
    @Select("SELECT COUNT(1) FROM report_permission rp " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND rp.template_id = #{templateId} " +
            "AND rp.permission_type >= #{permissionType}")
    int checkUserPermission(@Param("userId") Long userId, @Param("templateId") Long templateId,
                           @Param("permissionType") Integer permissionType);

    /**
     * 获取用户的所有角色编码
     */
    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectUserRoleCodes(@Param("userId") Long userId);

    /**
     * 检查用户是否拥有指定角色编码
     */
    @Select("SELECT COUNT(1) FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.role_code = #{roleCode} AND r.status = 1")
    int checkUserHasRole(@Param("userId") Long userId, @Param("roleCode") String roleCode);
}
