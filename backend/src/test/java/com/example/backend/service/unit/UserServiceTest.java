package com.example.backend.service.unit;

import cn.hutool.crypto.SecureUtil;
import com.example.backend.dto.LoginDTO;
import com.example.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

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
    @DisplayName("密码加密验证")
    void passwordEncryption() {
        String password = "123456";
        String encrypted = SecureUtil.md5(password);

        assertNotNull(encrypted);
        assertEquals(32, encrypted.length());
        assertEquals(encrypted, SecureUtil.md5(password));
    }

    @Test
    @DisplayName("用户状态验证")
    void userStatusTest() {
        assertEquals(1, testUser.getStatus());

        testUser.setStatus(0);
        assertEquals(0, testUser.getStatus());
    }

    @Test
    @DisplayName("密码匹配验证")
    void passwordMatchTest() {
        String inputPassword = "123456";
        String encryptedInput = SecureUtil.md5(inputPassword);

        assertEquals(testUser.getPassword(), encryptedInput);

        String wrongPassword = "wrongpassword";
        String encryptedWrong = SecureUtil.md5(wrongPassword);
        assertNotEquals(testUser.getPassword(), encryptedWrong);
    }
}
