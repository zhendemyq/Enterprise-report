package com.example.backend.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色VO
 */
@Data
public class RoleVO {

    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    /**
     * 角色颜色
     */
    private String color;

    private Integer sort;

    private Integer status;

    /**
     * 是否系统角色（系统角色不允许删除）
     */
    private Boolean isSystem;

    /**
     * 拥有该角色的用户数量
     */
    private Integer userCount;

    private LocalDateTime createTime;
}
