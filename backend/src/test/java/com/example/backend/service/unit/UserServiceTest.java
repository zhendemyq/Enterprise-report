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
    @DisplayName("登录失败 - 用户不存在")
    void login_UserNotExist() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("nonexistent");
        loginDTO.setPassword("123456");

        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginDTO));
        assertEquals(ResultCode.USER_NOT_EXIST.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_WrongPassword() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("wrongpassword");

        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginDTO));
        assertEquals(ResultCode.USER_PASSWORD_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("登录失败 - 用户已禁用")
    void login_UserDisabled() {
        testUser.setStatus(0);
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");

        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login(loginDTO));
        assertEquals(ResultCode.USER_DISABLED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("密码加密验证")
    void passwordEncryption() {
        String password = "123456";
        String encrypted = SecureUtil.md5(password);

        assertNotNull(encrypted);
        assertEquals(32, encrypted.length());
        assertEquals(encrypted, SecureUtil.md5(password));
    }
}
