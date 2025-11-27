package com.example.backend.dto;

import lombok.Data;

/**
 * 报表模板查询DTO
 */
@Data
public class ReportTemplateQueryDTO extends PageDTO {

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板类型
     */
    private Integer templateType;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 状态
     */
    private Integer status;
}
