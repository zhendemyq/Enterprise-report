package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户角色关联Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 物理删除用户的所有角色关联（绕过逻辑删除）
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserIdPhysically(@Param("userId") Long userId);

    /**
     * 物理删除指定角色的所有用户关联（绕过逻辑删除）
     */
    @Delete("DELETE FROM sys_user_role WHERE role_id = #{roleId}")
    int deleteByRoleIdPhysically(@Param("roleId") Long roleId);
}
