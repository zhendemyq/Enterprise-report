package com.example.backend.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 定时任务VO
 */
@Data
public class ReportScheduleVO {

    private Long id;

    private String taskName;

    private String taskCode;

    private Long templateId;

    private String templateName;

    private Map<String, Object> generateParams;

    private String cronExpression;

    private Integer scheduleType;

    private String scheduleTypeName;

    private String fileType;

    private Boolean sendEmail;

    private String emailReceivers;

    private String emailSubject;

    private String emailContent;

    private Integer status;

    private String statusName;

    private LocalDateTime lastExecuteTime;

    private LocalDateTime nextExecuteTime;

    private Integer executeCount;

    private Integer failCount;

    private Integer xxlJobId;

    private String description;

    private LocalDateTime createTime;
}
