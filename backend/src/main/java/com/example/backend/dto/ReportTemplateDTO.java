package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 报表模板DTO
 */
@Data
public class ReportTemplateDTO {

    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    /**
     * 模板类型 1-明细表 2-汇总表 3-分组统计表 4-图表报表
     */
    private Integer templateType;

    /**
     * 模板分类ID
     */
    private Long categoryId;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板配置（Univer设计器JSON）
     */
    private Map<String, Object> templateConfig;

    /**
     * 模板文件路径
     */
    private String templateFile;

    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * 查询SQL
     */
    private String querySql;

    /**
     * 参数配置
     */
    private Map<String, Object> paramConfig;

    /**
     * 样式配置
     */
    private Map<String, Object> styleConfig;

    /**
     * 是否需要审核
     */
    private Boolean needAudit;

    /**
     * 排序
     */
    private Integer sort;
}
