package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 定时任务DTO
 */
@Data
public class ReportScheduleDTO {

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    /**
     * 任务编码
     */
    private String taskCode;

    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    /**
     * 生成参数
     */
    private Map<String, Object> generateParams;

    @NotBlank(message = "Cron表达式不能为空")
    private String cronExpression;

    /**
     * 调度类型 1-每日 2-每周 3-每月 4-自定义
     */
    private Integer scheduleType;

    /**
     * 文件类型
     */
    private String fileType = "xlsx";

    /**
     * 是否发送邮件
     */
    private Boolean sendEmail = false;

    /**
     * 收件人邮箱
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
     * 描述
     */
    private String description;
}
