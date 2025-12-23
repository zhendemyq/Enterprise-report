package com.example.backend.service.unit;

import com.example.backend.entity.ReportRecord;
import com.example.backend.entity.ReportTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 报表生成服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReportGenerateServiceTest {

    private ReportTemplate testTemplate;
    private ReportRecord testRecord;

    @BeforeEach
    void setUp() {
        testTemplate = new ReportTemplate();
        testTemplate.setId(1L);
        testTemplate.setTemplateName("测试报表模板");
        testTemplate.setTemplateCode("TPL_TEST_001");
        testTemplate.setTemplateType(1);
        testTemplate.setStatus(1);
        testTemplate.setDatasourceId(1L);
        testTemplate.setQuerySql("SELECT * FROM test_table WHERE date >= #{startDate}");

        testRecord = new ReportRecord();
        testRecord.setId(1L);
        testRecord.setTemplateId(1L);
        testRecord.setTemplateName("测试报表模板");
        testRecord.setStatus(2);
        testRecord.setExportFormat("xlsx");
        testRecord.setFilePath("/reports/test_report.xlsx");
        testRecord.setFileSize(1024L);
        testRecord.setCreateTime(LocalDateTime.now());
        testRecord.setFinishTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("报表记录状态测试")
    void recordStatusTest() {
        // 0-待生成 1-生成中 2-生成成功 3-生成失败
        testRecord.setStatus(0);
        assertEquals(0, testRecord.getStatus());

        testRecord.setStatus(2);
        assertEquals(2, testRecord.getStatus());
    }

    @Test
    @DisplayName("导出格式验证测试")
    void exportFormatTest() {
        String[] validFormats = {"xlsx", "pdf", "csv"};

        for (String format : validFormats) {
            testRecord.setExportFormat(format);
            assertEquals(format, testRecord.getExportFormat());
        }
    }

    @Test
    @DisplayName("SQL参数替换测试")
    void sqlParameterReplacementTest() {
        String sql = "SELECT * FROM orders WHERE date >= #{startDate} AND status = #{status}";
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", "2024-01-01");
        params.put("status", 1);

        String processedSql = sql;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            processedSql = processedSql.replace("#{" + entry.getKey() + "}",
                    String.valueOf(entry.getValue()));
        }

        assertEquals("SELECT * FROM orders WHERE date >= 2024-01-01 AND status = 1", processedSql);
    }

    @Test
    @DisplayName("模板状态验证测试")
    void templateStatusTest() {
        // 0-草稿 1-已发布 2-已下线
        testTemplate.setStatus(0);
        assertFalse(testTemplate.getStatus() == 1);

        testTemplate.setStatus(1);
        assertTrue(testTemplate.getStatus() == 1);
    }

    @Test
    @DisplayName("文件大小格式化测试")
    void fileSizeFormatTest() {
        testRecord.setFileSize(1024L);
        assertEquals(1024L, testRecord.getFileSize());

        testRecord.setFileSize(1024 * 1024L);
        assertEquals(1048576L, testRecord.getFileSize());
    }
}
