package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 角色DTO
 */
@Data
public class RoleDTO {

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色颜色
     */
    private String color;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 0-禁用 1-正常
     */
    private Integer status;
}
