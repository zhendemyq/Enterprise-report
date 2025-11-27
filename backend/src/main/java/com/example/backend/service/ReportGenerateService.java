package com.example.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.ReportGenerateDTO;
import com.example.backend.dto.ReportRecordQueryDTO;
import com.example.backend.entity.ReportRecord;
import com.example.backend.vo.ReportRecordVO;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 报表生成服务接口
 */
public interface ReportGenerateService extends IService<ReportRecord> {

    /**
     * 生成报表（同步）
     */
    ReportRecordVO generateReport(ReportGenerateDTO generateDTO);

    /**
     * 生成报表（异步）
     */
    Long generateReportAsync(ReportGenerateDTO generateDTO);

    /**
     * 预览报表
     */
    void previewReport(Long recordId, HttpServletResponse response);

    /**
     * 下载报表
     */
    void downloadReport(Long recordId, HttpServletResponse response);

    /**
     * 查询生成记录
     */
    IPage<ReportRecordVO> pageRecords(ReportRecordQueryDTO queryDTO);

    /**
     * 获取记录详情
     */
    ReportRecordVO getRecordDetail(Long id);

    /**
     * 删除生成记录
     */
    void deleteRecord(Long id);

    /**
     * 重新生成报表
     */
    ReportRecordVO regenerateReport(Long recordId);
}
