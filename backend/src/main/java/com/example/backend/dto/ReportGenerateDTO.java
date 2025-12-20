package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 报表生成请求DTO
 */
@Data
@Schema(description = "报表生成请求参数")
public class ReportGenerateDTO {

    @Schema(description = "模板ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    @Schema(description = "报表名称", example = "2024年1月销售报表")
    private String reportName;

    @Schema(description = "生成参数")
    private Map<String, Object> params;

    @Schema(description = "文件类型", example = "xlsx", allowableValues = {"xlsx", "pdf"})
    private String fileType = "xlsx";

    @Schema(description = "是否异步生成", example = "false")
    private Boolean async = false;
}
