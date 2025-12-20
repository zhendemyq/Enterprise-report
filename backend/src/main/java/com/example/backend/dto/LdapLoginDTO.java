package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * LDAP登录请求DTO
 */
@Data
public class LdapLoginDTO {

    /**
     * LDAP用户名（可以是DN或sAMAccountName）
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * LDAP密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * LDAP域（可选，用于多域环境）
     */
    private String domain;
}
