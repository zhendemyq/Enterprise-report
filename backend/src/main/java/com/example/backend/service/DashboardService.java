package com.example.backend.service;

import com.example.backend.vo.DashboardStatsVO;
import com.example.backend.vo.ReportRecordVO;
import com.example.backend.vo.ReportTemplateVO;
import com.example.backend.vo.ReportTrendVO;

import java.util.List;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取统计数据
     */
    DashboardStatsVO getStats();

    /**
     * 获取最近生成的报表
     */
    List<ReportRecordVO> getRecentReports(int limit);

    /**
     * 获取热门模板
     */
    List<ReportTemplateVO> getPopularTemplates(int limit);

    /**
     * 获取报表生成趋势
     */
    ReportTrendVO getReportTrend(String period);
}
