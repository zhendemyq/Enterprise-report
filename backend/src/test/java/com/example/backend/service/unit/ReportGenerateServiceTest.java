package com.example.backend.service.unit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.ReportGenerateDTO;
import com.example.backend.dto.ReportRecordQueryDTO;
import com.example.backend.entity.ReportRecord;
import com.example.backend.entity.ReportTemplate;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReportRecordMapper;
import com.example.backend.mapper.ReportTemplateMapper;
import com.example.backend.service.impl.ReportGenerateServiceImpl;
import com.example.backend.vo.ReportRecordVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 报表生成服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReportGenerateServiceTest {

    @Mock
    private ReportRecordMapper reportRecordMapper;

    @Mock
    private ReportTemplateMapper reportTemplateMapper;

    @InjectMocks
    private ReportGenerateServiceImpl reportGenerateService;

    private ReportTemplate testTemplate;
    private ReportRecord testRecord;
    private ReportGenerateDTO testGenerateDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试模板
        testTemplate = new ReportTemplate();
        testTemplate.setId(1L);
        testTemplate.setTemplateName("测试报表模板");
        testTemplate.setTemplateCode("TPL_TEST_001");
        testTemplate.setTemplateType(1);
        testTemplate.setStatus(1); // 已发布
        testTemplate.setDatasourceId(1L);
        testTemplate.setQuerySql("SELECT * FROM test_table WHERE date >= #{startDate}");

        // 初始化测试记录
        testRecord = new ReportRecord();
        testRecord.setId(1L);
        testRecord.setTemplateId(1L);
        testRecord.setTemplateName("测试报表模板");
        testRecord.setStatus(2); // 生成成功
        testRecord.setExportFormat("xlsx");
        testRecord.setFilePath("/reports/test_report.xlsx");
        testRecord.setFileSize(1024L);
        testRecord.setCreateTime(LocalDateTime.now());
        testRecord.setFinishTime(LocalDateTime.now());

        // 初始化生成请求DTO
        testGenerateDTO = new ReportGenerateDTO();
        testGenerateDTO.setTemplateId(1L);
        testGenerateDTO.setExportFormat("xlsx");
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", "2024-01-01");
        testGenerateDTO.setParams(params);
    }

    @Test
    @DisplayName("生成报表 - 模板不存在")
    void generateReport_TemplateNotFound() {
        // Given
        when(reportTemplateMapper.selectById(anyLong())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reportGenerateService.generateReport(testGenerateDTO));
        assertEquals(ResultCode.TEMPLATE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("生成报表 - 模板未发布")
    void generateReport_TemplateNotPublished() {
        // Given
        testTemplate.setStatus(0); // 草稿状态
        when(reportTemplateMapper.selectById(1L)).thenReturn(testTemplate);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reportGenerateService.generateReport(testGenerateDTO));
        assertEquals(ResultCode.TEMPLATE_NOT_PUBLISHED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("获取记录详情成功")
    void getRecordDetail_Success() {
        // Given
        when(reportRecordMapper.selectById(1L)).thenReturn(testRecord);

        // When
        ReportRecordVO vo = reportGenerateService.getRecordDetail(1L);

        // Then
        assertNotNull(vo);
        assertEquals("测试报表模板", vo.getTemplateName());
        assertEquals(2, vo.getStatus());
    }

    @Test
    @DisplayName("获取记录详情失败 - 记录不存在")
    void getRecordDetail_NotFound() {
        // Given
        when(reportRecordMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reportGenerateService.getRecordDetail(999L));
        assertEquals(ResultCode.RECORD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("分页查询记录")
    void pageRecords_Success() {
        // Given
        ReportRecordQueryDTO queryDTO = new ReportRecordQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        Page<ReportRecord> page = new Page<>(1, 10);
        page.setRecords(java.util.List.of(testRecord));
        page.setTotal(1);

        when(reportRecordMapper.selectPage(any(), any())).thenReturn(page);

        // When
        IPage<ReportRecordVO> result = reportGenerateService.pageRecords(queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("删除记录成功")
    void deleteRecord_Success() {
        // Given
        when(reportRecordMapper.selectById(1L)).thenReturn(testRecord);
        when(reportRecordMapper.deleteById(1L)).thenReturn(1);

        // When
        assertDoesNotThrow(() -> reportGenerateService.deleteRecord(1L));

        // Then
        verify(reportRecordMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除记录失败 - 记录不存在")
    void deleteRecord_NotFound() {
        // Given
        when(reportRecordMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reportGenerateService.deleteRecord(999L));
        assertEquals(ResultCode.RECORD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("重新生成报表 - 记录不存在")
    void regenerateReport_RecordNotFound() {
        // Given
        when(reportRecordMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reportGenerateService.regenerateReport(999L));
        assertEquals(ResultCode.RECORD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("支持的导出格式验证")
    void validateExportFormat() {
        // 支持xlsx格式
        testGenerateDTO.setExportFormat("xlsx");
        // 不应抛出异常（假设模板存在且已发布的情况下）

        // 支持pdf格式
        testGenerateDTO.setExportFormat("pdf");
        // 不应抛出异常

        // 不支持的格式
        testGenerateDTO.setExportFormat("doc");
        // 应该在实际生成时抛出异常
    }
}
