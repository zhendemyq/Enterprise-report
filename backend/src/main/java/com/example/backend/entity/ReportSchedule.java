package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 定时报表任务实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "report_schedule", autoResultMap = true)
public class ReportSchedule extends BaseEntity {

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务编码
     */
    private String taskCode;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 生成参数（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> generateParams;

    /**
     * Cron表达式
     */
    private String cronExpression;

    /**
     * 调度类型 1-每日 2-每周 3-每月 4-自定义
     */
    private Integer scheduleType;

    /**
     * 文件类型 xlsx/pdf
     */
    private String fileType;

    /**
     * 是否发送邮件
     */
    private Boolean sendEmail;

    /**
     * 收件人邮箱（多个用逗号分隔）
     */
    private String emailReceivers;

    /**
     * 邮件主题
     */
    private String emailSubject;

    /**
     * 邮件内容
     */
    private String emailContent;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 上次执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;

    /**
     * 执行次数
     */
    private Integer executeCount;

    /**
     * 失败次数
     */
    private Integer failCount;

    /**
     * XXL-JOB任务ID
     */
    private Integer xxlJobId;

    /**
     * 描述
     */
    private String description;
}
