package com.example.backend.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    
    // 参数错误 4xx
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    // 业务错误 5xx
    USER_NOT_EXIST(5001, "用户不存在"),
    USER_PASSWORD_ERROR(5002, "密码错误"),
    USER_DISABLED(5003, "用户已禁用"),
    USER_ALREADY_EXIST(5004, "用户已存在"),
    
    // 报表相关错误
    TEMPLATE_NOT_FOUND(6001, "报表模板不存在"),
    TEMPLATE_FILE_NOT_FOUND(6002, "模板文件不存在"),
    TEMPLATE_PARSE_ERROR(6003, "模板解析失败"),
    DATASOURCE_ERROR(6004, "数据源连接失败"),
    REPORT_GENERATE_ERROR(6005, "报表生成失败"),
    REPORT_EXPORT_ERROR(6006, "报表导出失败"),
    REPORT_TOO_LARGE(6007, "报表数据量过大"),
    PARAM_CONFIG_ERROR(6008, "参数配置错误"),
    PDF_CONVERT_ERROR(6009, "PDF转换失败"),
    
    // 权限错误
    NO_PERMISSION(7001, "无操作权限"),
    ROLE_NOT_FOUND(7002, "角色不存在"),
    
    // 任务相关
    TASK_NOT_FOUND(8001, "任务不存在"),
    TASK_EXECUTE_ERROR(8002, "任务执行失败"),
    
    // 模板状态相关
    TEMPLATE_NOT_PUBLISHED(6010, "模板未发布"),
    RECORD_NOT_FOUND(6011, "报表记录不存在"),
    
    // 定时任务相关
    SCHEDULE_NOT_FOUND(8003, "定时任务不存在"),
    SCHEDULE_DISABLED(8004, "定时任务已禁用"),
    INVALID_CRON_EXPRESSION(8005, "Cron表达式无效"),
    
    // SSO/LDAP相关
    SSO_NOT_ENABLED(9001, "SSO功能未启用"),
    SSO_NOT_IMPLEMENTED(9002, "SSO功能尚未实现"),
    SSO_PROVIDER_NOT_SUPPORTED(9003, "不支持的SSO提供商类型"),
    SSO_TOKEN_INVALID(9004, "SSO Token无效"),
    LDAP_NOT_ENABLED(9101, "LDAP功能未启用"),
    LDAP_NOT_IMPLEMENTED(9102, "LDAP功能尚未实现"),
    LDAP_AUTH_FAILED(9103, "LDAP认证失败"),
    LDAP_CONNECTION_ERROR(9104, "LDAP连接失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
