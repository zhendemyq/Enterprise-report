package com.example.backend.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息VO
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Integer gender;

    private Long deptId;

    private String deptName;

    private Integer status;

    private String remark;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private LocalDateTime createTime;

    /**
     * 角色列表
     */
    private List<RoleVO> roles;
}
