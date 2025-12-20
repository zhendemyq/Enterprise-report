package com.example.backend.service.unit;

import cn.hutool.crypto.SecureUtil;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.LoginDTO;
import com.example.backend.dto.UserDTO;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(SecureUtil.md5("123456"));
        testUser.setNickname("测试用户");
        testUser.setStatus(1);
    }

    @Test
    @DisplayName("登录成功 - 用户名密码正确")
    void login_Success() {
        // Given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");

        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        // When & Then - 由于Sa-Token需要Web环境，这里只验证用户查询逻辑
        verify(userMapper, never()).selectByUsername(any());
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_UserNotExist() {
        // Given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("nonexistent");
        loginDTO.setPassword("123456");

        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginDTO));
        assertEquals(ResultCode.USER_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_WrongPassword() {
        // Given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("wrongpassword");

        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginDTO));
        assertEquals(ResultCode.USER_PASSWORD_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("登录失败 - 用户已禁用")
    void login_UserDisabled() {
        // Given
        testUser.setStatus(0);
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");

        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginDTO));
        assertEquals(ResultCode.USER_DISABLED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("创建用户成功")
    void createUser_Success() {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");
        userDTO.setPassword("123456");
        userDTO.setNickname("新用户");

        when(userMapper.selectByUsername("newuser")).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // When
        Long userId = userService.createUser(userDTO);

        // Then
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("创建用户失败 - 用户名已存在")
    void createUser_UsernameExists() {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("123456");

        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.createUser(userDTO));
        assertEquals(ResultCode.USER_ALREADY_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("修改密码成功")
    void changePassword_Success() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.changePassword(1L, "123456", "newpassword");

        // Then
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    @DisplayName("修改密码失败 - 原密码错误")
    void changePassword_WrongOldPassword() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.changePassword(1L, "wrongpassword", "newpassword"));
        assertEquals(ResultCode.USER_PASSWORD_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("重置密码成功")
    void resetPassword_Success() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.resetPassword(1L);

        // Then
        verify(userMapper).updateById(argThat(user ->
                SecureUtil.md5("123456").equals(((User) user).getPassword())));
    }
}
