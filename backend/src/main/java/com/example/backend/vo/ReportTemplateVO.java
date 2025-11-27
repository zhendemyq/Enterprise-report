package com.example.backend.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 报表模板VO
 */
@Data
public class ReportTemplateVO {

    private Long id;

    private String templateName;

    private String templateCode;

    private Integer templateType;

    private String templateTypeName;

    private Long categoryId;

    private String categoryName;

    private String description;

    private Map<String, Object> templateConfig;

    private String templateFile;

    private Long datasourceId;

    private String datasourceName;

    private String querySql;

    private Map<String, Object> paramConfig;

    private Map<String, Object> styleConfig;

    private Integer status;

    private String statusName;

    private Integer version;

    private Boolean needAudit;

    private Integer sort;

    private String createByName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
