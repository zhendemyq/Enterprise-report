package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 定时任务执行日志实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "report_schedule_log", autoResultMap = true)
public class ReportScheduleLog extends BaseEntity {

    /**
     * 任务ID
     */
    private Long scheduleId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 执行参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> executeParams;

    /**
     * 执行状态 0-执行中 1-成功 2-失败
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 生成的报表ID
     */
    private Long reportRecordId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时（毫秒）
     */
    private Long duration;
}
