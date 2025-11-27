package com.example.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.LoginDTO;
import com.example.backend.dto.UserDTO;
import com.example.backend.dto.UserQueryDTO;
import com.example.backend.entity.User;
import com.example.backend.vo.LoginVO;
import com.example.backend.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户注销
     */
    void logout();

    /**
     * 获取当前登录用户信息
     */
    UserVO getCurrentUser();

    /**
     * 创建用户
     */
    Long createUser(UserDTO userDTO);

    /**
     * 更新用户
     */
    void updateUser(Long id, UserDTO userDTO);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 分页查询用户
     */
    IPage<UserVO> pageUsers(UserQueryDTO queryDTO);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    void resetPassword(Long userId);
}
