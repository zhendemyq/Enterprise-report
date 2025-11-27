package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 报表生成请求DTO
 */
@Data
public class ReportGenerateDTO {

    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    /**
     * 报表名称
     */
    private String reportName;

    /**
     * 生成参数
     */
    private Map<String, Object> params;

    /**
     * 文件类型 xlsx/pdf
     */
    private String fileType = "xlsx";

    /**
     * 是否异步生成
     */
    private Boolean async = false;
}
