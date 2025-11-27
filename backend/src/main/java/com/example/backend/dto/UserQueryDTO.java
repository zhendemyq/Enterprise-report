package com.example.backend.dto;

import lombok.Data;

/**
 * 用户查询DTO
 */
@Data
public class UserQueryDTO extends PageDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 部门ID
     */
    private Long deptId;
}
