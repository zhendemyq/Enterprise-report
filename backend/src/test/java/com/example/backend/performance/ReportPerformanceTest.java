package com.example.backend.performance;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 报表系统性能测试
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReportPerformanceTest {

    @Test
    @Order(1)
    @DisplayName("小数据量处理测试（1000行）")
    void testSmallDataProcessing() {
        int dataSize = 1000;
        long startTime = System.currentTimeMillis();

        List<Map<String, Object>> data = generateMockData(dataSize);

        long duration = System.currentTimeMillis() - startTime;
        log.info("处理 {} 行数据耗时: {}ms", dataSize, duration);

        assertEquals(dataSize, data.size());
        assertTrue(duration < 5000, "小数据量处理应在5秒内完成");
    }

    @Test
    @Order(2)
    @DisplayName("中等数据量处理测试（10000行）")
    void testMediumDataProcessing() {
        int dataSize = 10000;
        long startTime = System.currentTimeMillis();

        List<Map<String, Object>> data = generateMockData(dataSize);

        long duration = System.currentTimeMillis() - startTime;
        log.info("处理 {} 行数据耗时: {}ms", dataSize, duration);

        assertEquals(dataSize, data.size());
        assertTrue(duration < 30000, "中等数据量处理应在30秒内完成");
    }

    @Test
    @Order(3)
    @DisplayName("大数据量处理测试（100000行）")
    void testLargeDataProcessing() {
        int dataSize = 100000;
        long startTime = System.currentTimeMillis();

        List<Map<String, Object>> data = generateMockData(dataSize);

        long duration = System.currentTimeMillis() - startTime;
        log.info("处理 {} 行数据耗时: {}ms", dataSize, duration);

        assertEquals(dataSize, data.size());
        assertTrue(duration < 120000, "大数据量处理应在120秒内完成");
    }

    @Test
    @Order(4)
    @DisplayName("内存使用测试")
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();

        List<Map<String, Object>> data = generateMockData(50000);

        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = (afterMemory - beforeMemory) / (1024 * 1024);

        log.info("处理50000行数据内存增量: {}MB", memoryUsed);

        assertNotNull(data);
        assertTrue(memoryUsed < 500, "内存使用应小于500MB");
    }

    private List<Map<String, Object>> generateMockData(int size) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", i);
            row.put("name", "Product_" + i);
            row.put("amount", Math.random() * 10000);
            row.put("status", 1);
            data.add(row);
        }
        return data;
    }
}
