package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * SSO登录请求DTO
 */
@Data
public class SsoLoginDTO {

    /**
     * SSO Token
     */
    @NotBlank(message = "SSO Token不能为空")
    private String ssoToken;

    /**
     * SSO提供商类型 (cas, oauth2, saml, etc.)
     */
    private String providerType;

    /**
     * 回调URL
     */
    private String callbackUrl;

    /**
     * 额外参数
     */
    private String extraParams;
}
