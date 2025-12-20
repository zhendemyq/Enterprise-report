package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报表邮件发送请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEmailRequest {

    /**
     * 收件人列表
     */
    @NotEmpty(message = "收件人列表不能为空")
    private List<String> recipients;

    /**
     * 邮件主题
     */
    @NotBlank(message = "邮件主题不能为空")
    private String subject;

    /**
     * 报表名称
     */
    @NotBlank(message = "报表名称不能为空")
    private String reportName;

    /**
     * 生成时间
     */
    private LocalDateTime generateTime;

    /**
     * 数据行数
     */
    private Integer dataRows;

    /**
     * 附件路径
     */
    private String attachmentPath;

    /**
     * 附件名称
     */
    private String attachmentName;
}
