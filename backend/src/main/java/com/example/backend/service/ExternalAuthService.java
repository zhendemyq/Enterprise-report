package com.example.backend.service;

import com.example.backend.dto.LdapLoginDTO;
import com.example.backend.dto.SsoLoginDTO;
import com.example.backend.vo.LoginVO;

/**
 * 外部认证服务接口
 * 
 * 预留接口，用于支持SSO单点登录和LDAP/AD域认证
 * 后续可根据企业实际需求进行实现
 * 
 * @author Enterprise Report System
 * @since 1.0.0
 */
public interface ExternalAuthService {

    /**
     * SSO单点登录认证
     * 
     * 支持的SSO类型:
     * - CAS (Central Authentication Service)
     * - OAuth 2.0
     * - SAML 2.0
     * - OpenID Connect
     * 
     * @param ssoLoginDTO SSO登录参数
     * @return 登录结果（包含token和用户信息）
     */
    LoginVO ssoLogin(SsoLoginDTO ssoLoginDTO);

    /**
     * SSO登录重定向URL
     * 
     * 获取跳转到SSO登录页面的URL
     * 
     * @param providerType SSO提供商类型
     * @param callbackUrl 回调URL
     * @return SSO登录页面URL
     */
    String getSsoLoginUrl(String providerType, String callbackUrl);

    /**
     * SSO登出
     * 
     * 通知SSO服务器用户已登出
     * 
     * @param ssoToken SSO Token
     */
    void ssoLogout(String ssoToken);

    /**
     * LDAP/Active Directory认证
     * 
     * 支持:
     * - OpenLDAP
     * - Microsoft Active Directory
     * - 其他兼容LDAP协议的目录服务
     * 
     * @param ldapLoginDTO LDAP登录参数
     * @return 登录结果（包含token和用户信息）
     */
    LoginVO ldapLogin(LdapLoginDTO ldapLoginDTO);

    /**
     * 验证LDAP连接配置
     * 
     * @return 连接是否成功
     */
    boolean testLdapConnection();

    /**
     * 同步LDAP用户到本地数据库
     * 
     * 从LDAP服务器同步用户信息到本地用户表
     * 用于定期同步或首次集成时的批量导入
     * 
     * @param groupDn 要同步的LDAP组DN（可选）
     * @return 同步的用户数量
     */
    int syncLdapUsers(String groupDn);

    /**
     * 检查SSO是否启用
     * 
     * @return SSO是否启用
     */
    boolean isSsoEnabled();

    /**
     * 检查LDAP是否启用
     * 
     * @return LDAP是否启用
     */
    boolean isLdapEnabled();

    /**
     * 获取支持的SSO提供商列表
     * 
     * @return 提供商类型列表
     */
    java.util.List<String> getSupportedSsoProviders();
}
