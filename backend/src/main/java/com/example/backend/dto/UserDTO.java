package com.example.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 用户DTO
 */
@Data
public class UserDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private String avatar;

    private Integer gender;

    private Long deptId;

    private Integer status;

    private String remark;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
}
