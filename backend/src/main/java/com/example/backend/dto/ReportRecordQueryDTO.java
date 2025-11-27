package com.example.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报表记录查询DTO
 */
@Data
public class ReportRecordQueryDTO extends PageDTO {

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
