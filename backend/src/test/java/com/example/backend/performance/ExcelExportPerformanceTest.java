package com.example.backend.performance;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Excel导出性能测试
 * 
 * 测试目标：验证10万行数据Excel导出的性能
 * 
 * @author Enterprise Report System
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExcelExportPerformanceTest {

    private static final String TEST_OUTPUT_DIR = "./test-output/performance/";
    
    private static final Map<String, ExportResult> results = new LinkedHashMap<>();

    @BeforeAll
    static void beforeAll() {
        // 创建输出目录
        File dir = new File(TEST_OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        log.info("===========================================");
        log.info("       Excel导出性能测试开始");
        log.info("===========================================");
    }

    @AfterAll
    static void afterAll() {
        log.info("===========================================");
        log.info("       Excel导出性能测试结果汇总");
        log.info("===========================================");
        
        for (Map.Entry<String, ExportResult> entry : results.entrySet()) {
            ExportResult result = entry.getValue();
            log.info("测试项: {}", entry.getKey());
            log.info("  - 数据行数: {} 行", result.rowCount);
            log.info("  - 文件大小: {} KB", result.fileSizeKb);
            log.info("  - 导出耗时: {} ms", result.exportTimeMs);
            log.info("  - 吞吐量: {} 行/秒", result.throughput);
            log.info("  - 状态: {}", result.success ? "成功" : "失败");
            log.info("-------------------------------------------");
        }
    }

    /**
     * 测试1：小数据量Excel导出（1000行）
     */
    @Test
    @Order(1)
    @DisplayName("小数据量Excel导出（1000行）")
    void testSmallExcelExport() {
        performExportTest("小数据量导出", 1000, 10);
    }

    /**
     * 测试2：中等数据量Excel导出（10000行）
     */
    @Test
    @Order(2)
    @DisplayName("中等数据量Excel导出（10000行）")
    void testMediumExcelExport() {
        performExportTest("中等数据量导出", 10000, 20);
    }

    /**
     * 测试3：大数据量Excel导出（100000行）
     */
    @Test
    @Order(3)
    @DisplayName("大数据量Excel导出（100000行）")
    void testLargeExcelExport() {
        performExportTest("大数据量导出", 100000, 30);
    }

    /**
     * 测试4：多Sheet导出性能
     */
    @Test
    @Order(4)
    @DisplayName("多Sheet导出性能测试")
    void testMultiSheetExport() {
        int sheetsCount = 5;
        int rowsPerSheet = 20000;
        
        log.info("开始多Sheet导出测试: {} 个Sheet, 每个 {} 行", sheetsCount, rowsPerSheet);
        
        String fileName = TEST_OUTPUT_DIR + "multi_sheet_test.xlsx";
        long startTime = System.currentTimeMillis();
        
        try (ExcelWriter excelWriter = EasyExcel.write(fileName, ReportDataRow.class).build()) {
            for (int sheetNum = 0; sheetNum < sheetsCount; sheetNum++) {
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetNum, "Sheet" + (sheetNum + 1)).build();
                List<ReportDataRow> data = generateTestData(rowsPerSheet, 20);
                excelWriter.write(data, writeSheet);
                log.info("Sheet {} 导出完成", sheetNum + 1);
            }
        }
        
        long exportTimeMs = System.currentTimeMillis() - startTime;
        File file = new File(fileName);
        
        ExportResult result = new ExportResult();
        result.rowCount = sheetsCount * rowsPerSheet;
        result.fileSizeKb = file.length() / 1024;
        result.exportTimeMs = exportTimeMs;
        result.throughput = (long) (result.rowCount * 1000.0 / exportTimeMs);
        result.success = file.exists();
        
        results.put("多Sheet导出", result);
        
        log.info("多Sheet导出完成: 总行数={}, 耗时={}ms, 文件大小={}KB",
                result.rowCount, exportTimeMs, result.fileSizeKb);
        
        // 清理测试文件
        file.delete();
    }

    /**
     * 测试5：分批写入性能（流式写入）
     */
    @Test
    @Order(5)
    @DisplayName("分批写入性能测试（流式写入）")
    void testBatchWritePerformance() {
        int totalRows = 100000;
        int batchSize = 5000;
        int batches = totalRows / batchSize;
        
        log.info("开始分批写入测试: 总行数={}, 批次大小={}, 批次数={}", totalRows, batchSize, batches);
        
        String fileName = TEST_OUTPUT_DIR + "batch_write_test.xlsx";
        long startTime = System.currentTimeMillis();
        
        try (ExcelWriter excelWriter = EasyExcel.write(fileName, ReportDataRow.class).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("数据").build();
            
            for (int batch = 0; batch < batches; batch++) {
                List<ReportDataRow> batchData = generateTestData(batchSize, 15);
                excelWriter.write(batchData, writeSheet);
                
                if ((batch + 1) % 5 == 0) {
                    log.info("已完成 {}/{} 批次", batch + 1, batches);
                }
            }
        }
        
        long exportTimeMs = System.currentTimeMillis() - startTime;
        File file = new File(fileName);
        
        ExportResult result = new ExportResult();
        result.rowCount = totalRows;
        result.fileSizeKb = file.length() / 1024;
        result.exportTimeMs = exportTimeMs;
        result.throughput = (long) (totalRows * 1000.0 / exportTimeMs);
        result.success = file.exists();
        
        results.put("分批写入", result);
        
        log.info("分批写入完成: 耗时={}ms, 文件大小={}KB, 吞吐量={} 行/秒",
                exportTimeMs, result.fileSizeKb, result.throughput);
        
        // 性能断言
        Assertions.assertTrue(exportTimeMs < 60000, "10万行分批写入应在60秒内完成");
        
        // 清理测试文件
        file.delete();
    }

    /**
     * 执行导出性能测试
     */
    private void performExportTest(String testName, int rowCount, int columnCount) {
        log.info("开始{}测试: {} 行 x {} 列", testName, rowCount, columnCount);
        
        String fileName = TEST_OUTPUT_DIR + testName.replace(" ", "_") + ".xlsx";
        long startTime = System.currentTimeMillis();
        
        // 生成测试数据
        List<ReportDataRow> testData = generateTestData(rowCount, columnCount);
        long dataGenTime = System.currentTimeMillis() - startTime;
        log.info("数据生成耗时: {}ms", dataGenTime);
        
        // 导出Excel
        long exportStart = System.currentTimeMillis();
        EasyExcel.write(fileName, ReportDataRow.class)
                .sheet("数据")
                .doWrite(testData);
        long exportTimeMs = System.currentTimeMillis() - exportStart;
        
        File file = new File(fileName);
        
        ExportResult result = new ExportResult();
        result.rowCount = rowCount;
        result.fileSizeKb = file.length() / 1024;
        result.exportTimeMs = exportTimeMs;
        result.throughput = (long) (rowCount * 1000.0 / Math.max(exportTimeMs, 1));
        result.success = file.exists() && file.length() > 0;
        
        results.put(testName, result);
        
        log.info("{}完成: 耗时={}ms, 文件大小={}KB, 吞吐量={} 行/秒",
                testName, exportTimeMs, result.fileSizeKb, result.throughput);
        
        // 性能断言
        if (rowCount <= 1000) {
            Assertions.assertTrue(exportTimeMs < 5000, "1000行导出应在5秒内完成");
        } else if (rowCount <= 10000) {
            Assertions.assertTrue(exportTimeMs < 15000, "1万行导出应在15秒内完成");
        } else {
            Assertions.assertTrue(exportTimeMs < 60000, "10万行导出应在60秒内完成");
        }
        
        // 清理测试文件
        file.delete();
    }

    /**
     * 生成测试数据
     */
    private List<ReportDataRow> generateTestData(int rowCount, int columnCount) {
        List<ReportDataRow> data = new ArrayList<>(rowCount);
        Random random = new Random();
        
        for (int i = 0; i < rowCount; i++) {
            ReportDataRow row = new ReportDataRow();
            row.setId((long) (i + 1));
            row.setOrderNo("ORD" + String.format("%08d", i + 1));
            row.setProductName("产品_" + (i % 100));
            row.setCategory("分类_" + (i % 20));
            row.setQuantity(random.nextInt(100) + 1);
            row.setUnitPrice(BigDecimal.valueOf(random.nextDouble() * 1000).setScale(2, BigDecimal.ROUND_HALF_UP));
            row.setTotalAmount(row.getUnitPrice().multiply(BigDecimal.valueOf(row.getQuantity())));
            row.setCustomerName("客户_" + (i % 500));
            row.setRegion("区域_" + (i % 10));
            row.setStatus(i % 4);
            row.setCreateTime(new Date());
            row.setRemark("备注信息_" + i);
            data.add(row);
        }
        
        return data;
    }

    /**
     * 测试数据行模型
     */
    @Data
    public static class ReportDataRow {
        private Long id;
        private String orderNo;
        private String productName;
        private String category;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        private String customerName;
        private String region;
        private Integer status;
        private Date createTime;
        private String remark;
    }

    /**
     * 导出结果
     */
    private static class ExportResult {
        int rowCount;
        long fileSizeKb;
        long exportTimeMs;
        long throughput;
        boolean success;
    }
}
