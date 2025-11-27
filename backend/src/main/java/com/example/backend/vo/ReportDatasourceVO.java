package com.example.backend.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 数据源VO
 */
@Data
public class ReportDatasourceVO {

    private Long id;

    private String datasourceName;

    private String datasourceCode;

    private Integer datasourceType;

    private String datasourceTypeName;

    private String host;

    private Integer port;

    private String databaseName;

    private String username;

    private Map<String, Object> connectionParams;

    private String apiUrl;

    private Map<String, Object> apiHeaders;

    private String description;

    private Integer status;

    private String statusName;

    private LocalDateTime lastTestTime;

    private Integer testResult;

    private String testResultName;

    private LocalDateTime createTime;
}
