package com.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.dto.ReportScheduleDTO;
import com.example.backend.dto.ReportScheduleQueryDTO;
import com.example.backend.entity.ReportScheduleLog;
import com.example.backend.service.ReportScheduleService;
import com.example.backend.service.impl.ReportScheduleServiceImpl;
import com.example.backend.vo.ReportScheduleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 定时报表任务控制器
 */
@Tag(name = "定时任务管理", description = "定时任务CRUD、启用禁用、立即执行等接口")
@RestController
@RequestMapping("/report/schedule")
public class ReportScheduleController {

    @Autowired
    private ReportScheduleService reportScheduleService;

    @Autowired
    private ReportScheduleServiceImpl reportScheduleServiceImpl;

    @Operation(summary = "分页查询定时任务")
    @GetMapping("/page")
    public Result<PageResult<ReportScheduleVO>> pageSchedules(ReportScheduleQueryDTO queryDTO) {
        IPage<ReportScheduleVO> page = reportScheduleService.pageSchedules(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public Result<ReportScheduleVO> getScheduleDetail(@PathVariable Long id) {
        return Result.success(reportScheduleService.getScheduleDetail(id));
    }

    @Operation(summary = "创建定时任务")
    @PostMapping
    public Result<Long> createSchedule(@Valid @RequestBody ReportScheduleDTO scheduleDTO) {
        return Result.success(reportScheduleService.createSchedule(scheduleDTO));
    }

    @Operation(summary = "更新定时任务")
    @PutMapping("/{id}")
    public Result<Void> updateSchedule(@PathVariable Long id, @Valid @RequestBody ReportScheduleDTO scheduleDTO) {
        reportScheduleService.updateSchedule(id, scheduleDTO);
        return Result.success();
    }

    @Operation(summary = "删除定时任务")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        reportScheduleService.deleteSchedule(id);
        return Result.success();
    }

    @Operation(summary = "切换任务状态")
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == 1) {
            reportScheduleService.enableSchedule(id);
        } else {
            reportScheduleService.disableSchedule(id);
        }
        return Result.success();
    }

    @Operation(summary = "立即执行任务")
    @PostMapping("/{id}/execute")
    public Result<Void> executeNow(@PathVariable Long id) {
        reportScheduleService.executeNow(id);
        return Result.success();
    }

    @Operation(summary = "获取执行日志")
    @GetMapping("/{id}/logs")
    public Result<PageResult<ReportScheduleLog>> getScheduleLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<ReportScheduleLog> page = reportScheduleServiceImpl.getScheduleLogs(id, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }
}
