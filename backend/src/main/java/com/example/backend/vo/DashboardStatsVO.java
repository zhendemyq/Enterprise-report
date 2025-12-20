package com.example.backend.vo;

import lombok.Data;

/**
 * 仪表盘统计数据VO
 */
@Data
public class DashboardStatsVO {

    /**
     * 报表模板总数
     */
    private Long templateCount;

    /**
     * 已生成报表数
     */
    private Long reportCount;

    /**
     * 数据源总数
     */
    private Long datasourceCount;

    /**
     * 定时任务数
     */
    private Long scheduleCount;

    /**
     * 今日生成报表数
     */
    private Long todayReportCount;

    /**
     * 本周生成报表数
     */
    private Long weekReportCount;

    /**
     * 本月生成报表数
     */
    private Long monthReportCount;
}
