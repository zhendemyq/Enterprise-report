package com.example.backend.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryDTO extends PageDTO {

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 状态
     */
    private Integer status;
}
