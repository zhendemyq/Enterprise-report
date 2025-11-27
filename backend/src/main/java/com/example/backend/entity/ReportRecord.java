package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 报表生成记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "report_record", autoResultMap = true)
public class ReportRecord extends BaseEntity {

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 报表名称
     */
    private String reportName;

    /**
     * 生成参数（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> generateParams;

    /**
     * 文件类型 xlsx/pdf
     */
    private String fileType;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 数据行数
     */
    private Integer dataRows;

    /**
     * 状态 0-生成中 1-成功 2-失败
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 生成开始时间
     */
    private LocalDateTime startTime;

    /**
     * 生成结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时（毫秒）
     */
    private Long duration;

    /**
     * 下载次数
     */
    private Integer downloadCount;
}
