package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.service.DashboardService;
import com.example.backend.vo.DashboardStatsVO;
import com.example.backend.vo.ReportRecordVO;
import com.example.backend.vo.ReportTemplateVO;
import com.example.backend.vo.ReportTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘控制器
 */
@Tag(name = "仪表盘", description = "统计数据、趋势图、最近报表等接口")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "获取统计数据")
    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        return Result.success(dashboardService.getStats());
    }

    @Operation(summary = "获取最近生成的报表")
    @GetMapping("/recent-reports")
    public Result<List<ReportRecordVO>> getRecentReports(
            @RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(dashboardService.getRecentReports(limit));
    }

    @Operation(summary = "获取热门模板")
    @GetMapping("/popular-templates")
    public Result<List<ReportTemplateVO>> getPopularTemplates(
            @RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(dashboardService.getPopularTemplates(limit));
    }

    @Operation(summary = "获取报表生成趋势")
    @GetMapping("/trend")
    public Result<ReportTrendVO> getReportTrend(
            @RequestParam(defaultValue = "week") String period) {
        return Result.success(dashboardService.getReportTrend(period));
    }
}
