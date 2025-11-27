package com.example.backend.vo;

import lombok.Data;

import java.util.List;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 用户信息
     */
    private UserVO userInfo;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 权限列表
     */
    private List<String> permissions;
}
