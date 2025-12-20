package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.LdapLoginDTO;
import com.example.backend.dto.SsoLoginDTO;
import com.example.backend.service.ExternalAuthService;
import com.example.backend.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 外部认证控制器
 * 
 * 提供SSO单点登录和LDAP认证的API接口
 * 
 * @author Enterprise Report System
 * @since 1.0.0
 */
@Tag(name = "外部认证", description = "SSO/LDAP认证接口")
@RestController
@RequestMapping("/auth/external")
@RequiredArgsConstructor
public class ExternalAuthController {

    private final ExternalAuthService externalAuthService;

    /**
     * SSO登录
     */
    @Operation(summary = "SSO登录", description = "使用SSO Token进行登录认证")
    @PostMapping("/sso/login")
    public Result<LoginVO> ssoLogin(@Valid @RequestBody SsoLoginDTO ssoLoginDTO) {
        LoginVO loginVO = externalAuthService.ssoLogin(ssoLoginDTO);
        return Result.success(loginVO);
    }

    /**
     * 获取SSO登录URL
     */
    @Operation(summary = "获取SSO登录URL", description = "获取跳转到SSO登录页面的URL")
    @GetMapping("/sso/login-url")
    public Result<String> getSsoLoginUrl(
            @RequestParam(defaultValue = "cas") String providerType,
            @RequestParam String callbackUrl) {
        String loginUrl = externalAuthService.getSsoLoginUrl(providerType, callbackUrl);
        return Result.success(loginUrl);
    }

    /**
     * SSO登出
     */
    @Operation(summary = "SSO登出", description = "通知SSO服务器用户已登出")
    @PostMapping("/sso/logout")
    public Result<Void> ssoLogout(@RequestParam String ssoToken) {
        externalAuthService.ssoLogout(ssoToken);
        return Result.success();
    }

    /**
     * LDAP登录
     */
    @Operation(summary = "LDAP登录", description = "使用LDAP/AD域账号进行登录认证")
    @PostMapping("/ldap/login")
    public Result<LoginVO> ldapLogin(@Valid @RequestBody LdapLoginDTO ldapLoginDTO) {
        LoginVO loginVO = externalAuthService.ldapLogin(ldapLoginDTO);
        return Result.success(loginVO);
    }

    /**
     * 测试LDAP连接
     */
    @Operation(summary = "测试LDAP连接", description = "测试LDAP服务器连接是否正常")
    @PostMapping("/ldap/test")
    public Result<Boolean> testLdapConnection() {
        boolean result = externalAuthService.testLdapConnection();
        return Result.success(result);
    }

    /**
     * 同步LDAP用户
     */
    @Operation(summary = "同步LDAP用户", description = "从LDAP服务器同步用户到本地数据库")
    @PostMapping("/ldap/sync")
    public Result<Integer> syncLdapUsers(@RequestParam(required = false) String groupDn) {
        int count = externalAuthService.syncLdapUsers(groupDn);
        return Result.success(count);
    }

    /**
     * 获取外部认证配置状态
     */
    @Operation(summary = "获取外部认证配置状态", description = "获取SSO和LDAP的启用状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> getAuthStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("ssoEnabled", externalAuthService.isSsoEnabled());
        status.put("ldapEnabled", externalAuthService.isLdapEnabled());
        status.put("supportedSsoProviders", externalAuthService.getSupportedSsoProviders());
        return Result.success(status);
    }

    /**
     * 获取支持的SSO提供商列表
     */
    @Operation(summary = "获取支持的SSO提供商列表", description = "获取系统支持的SSO认证类型")
    @GetMapping("/sso/providers")
    public Result<List<String>> getSupportedSsoProviders() {
        List<String> providers = externalAuthService.getSupportedSsoProviders();
        return Result.success(providers);
    }
}
