package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 数据源配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "report_datasource", autoResultMap = true)
public class ReportDatasource extends BaseEntity {

    /**
     * 数据源名称
     */
    private String datasourceName;

    /**
     * 数据源编码
     */
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
     * 密码（加密存储）
     */
    private String password;

    /**
     * 连接参数（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> connectionParams;

    /**
     * API地址（当类型为API时）
     */
    private String apiUrl;

    /**
     * API请求头（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> apiHeaders;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态 0-禁用 1-正常
     */
    private Integer status;

    /**
     * 最后测试时间
     */
    private java.time.LocalDateTime lastTestTime;

    /**
     * 测试结果 0-失败 1-成功
     */
    private Integer testResult;
}
