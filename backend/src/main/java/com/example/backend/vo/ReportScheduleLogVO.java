package com.example.backend.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 定时任务执行日志VO
 */
@Data
public class ReportScheduleLogVO {

    private Long id;

    private Long scheduleId;

    private String taskName;

    private Map<String, Object> executeParams;

    private Integer status;

    private String statusName;

    private String errorMsg;

    private Long reportRecordId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long duration;

    private LocalDateTime createTime;
}
