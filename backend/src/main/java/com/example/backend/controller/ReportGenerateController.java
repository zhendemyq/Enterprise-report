package com.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.dto.ReportGenerateDTO;
import com.example.backend.dto.ReportRecordQueryDTO;
import com.example.backend.service.ReportGenerateService;
import com.example.backend.vo.ReportRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 报表生成控制器
 */
@Tag(name = "报表生成", description = "报表生成、预览、下载等接口")
@RestController
@RequestMapping("/report/generate")
public class ReportGenerateController {

    @Autowired
    private ReportGenerateService generateService;

    @Operation(summary = "生成报表")
    @PostMapping
    public Result<ReportRecordVO> generateReport(@Valid @RequestBody ReportGenerateDTO generateDTO) {
        if (Boolean.TRUE.equals(generateDTO.getAsync())) {
            Long recordId = generateService.generateReportAsync(generateDTO);
            ReportRecordVO vo = new ReportRecordVO();
            vo.setId(recordId);
            vo.setStatus(0);
            vo.setStatusName("生成中");
            return Result.success("报表正在生成中", vo);
        }
        return Result.success(generateService.generateReport(generateDTO));
    }

    @Operation(summary = "预览报表")
    @GetMapping("/{recordId}/preview")
    public void previewReport(@PathVariable Long recordId, HttpServletResponse response) {
        generateService.previewReport(recordId, response);
    }

    @Operation(summary = "下载报表")
    @GetMapping("/{recordId}/download")
    public void downloadReport(@PathVariable Long recordId, HttpServletResponse response) {
        generateService.downloadReport(recordId, response);
    }

    @Operation(summary = "分页查询生成记录")
    @GetMapping("/records")
    public Result<PageResult<ReportRecordVO>> pageRecords(ReportRecordQueryDTO queryDTO) {
        IPage<ReportRecordVO> page = generateService.pageRecords(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "获取生成记录详情")
    @GetMapping("/records/{id}")
    public Result<ReportRecordVO> getRecordDetail(@PathVariable Long id) {
        return Result.success(generateService.getRecordDetail(id));
    }

    @Operation(summary = "删除生成记录")
    @DeleteMapping("/records/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        generateService.deleteRecord(id);
        return Result.success();
    }

    @Operation(summary = "重新生成报表")
    @PostMapping("/records/{recordId}/regenerate")
    public Result<ReportRecordVO> regenerateReport(@PathVariable Long recordId) {
        return Result.success(generateService.regenerateReport(recordId));
    }
}
