package com.example.backend.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 报表生成记录VO
 */
@Data
public class ReportRecordVO {

    private Long id;

    private Long templateId;

    private String templateName;

    private String reportName;

    private Map<String, Object> generateParams;

    private String fileType;

    private String filePath;

    private Long fileSize;

    private String fileSizeStr;

    private Integer dataRows;

    private Integer status;

    private String statusName;

    private String errorMsg;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long duration;

    private String durationStr;

    private Integer downloadCount;

    private String createByName;

    private LocalDateTime createTime;
}
