package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 数据源DTO
 */
@Data
public class ReportDatasourceDTO {

    @NotBlank(message = "数据源名称不能为空")
    private String datasourceName;

    @NotBlank(message = "数据源编码不能为空")
    private String datasourceCode;

    /**
     * 数据源类型 1-MySQL 2-PostgreSQL 3-Oracle 4-SQLServer 5-API接口
     */
    private Integer datasourceType;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 数据库名
     */
    private String databaseName;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 连接参数
     */
    private Map<String, Object> connectionParams;

    /**
     * API地址
     */
    private String apiUrl;

    /**
     * API请求头
     */
    private Map<String, Object> apiHeaders;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private Integer status;
}
