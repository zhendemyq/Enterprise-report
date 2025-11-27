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
    TEMPLATE_PARSE_ERROR(6002, "模板解析失败"),
    DATASOURCE_ERROR(6003, "数据源连接失败"),
    REPORT_GENERATE_ERROR(6004, "报表生成失败"),
    REPORT_EXPORT_ERROR(6005, "报表导出失败"),
    REPORT_TOO_LARGE(6006, "报表数据量过大"),
    PARAM_CONFIG_ERROR(6007, "参数配置错误"),
    
    // 权限错误
    NO_PERMISSION(7001, "无操作权限"),
    ROLE_NOT_FOUND(7002, "角色不存在"),
    
    // 任务相关
    TASK_NOT_FOUND(8001, "任务不存在"),
    TASK_EXECUTE_ERROR(8002, "任务执行失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
