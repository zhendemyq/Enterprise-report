package com.example.backend.dto;

import lombok.Data;

/**
 * 定时任务查询DTO
 */
@Data
public class ReportScheduleQueryDTO extends PageDTO {

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 调度类型
     */
    private Integer scheduleType;
}
