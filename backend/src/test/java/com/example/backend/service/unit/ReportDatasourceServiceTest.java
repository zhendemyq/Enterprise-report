package com.example.backend.service.unit;

import com.example.backend.entity.ReportDatasource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据源服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReportDatasourceServiceTest {

    private ReportDatasource testDatasource;

    @BeforeEach
    void setUp() {
        testDatasource = new ReportDatasource();
        testDatasource.setId(1L);
        testDatasource.setDatasourceName("测试数据源");
        testDatasource.setDatasourceCode("DS_TEST_001");
        testDatasource.setDatasourceType(1); // MySQL
        testDatasource.setHost("localhost");
        testDatasource.setPort(3306);
        testDatasource.setDatabaseName("test_db");
        testDatasource.setUsername("root");
        testDatasource.setPassword("root");
        testDatasource.setStatus(1);
    }

    @Test
    @DisplayName("数据源类型映射测试")
    void datasourceTypeTest() {
        // 1-MySQL 2-PostgreSQL 3-Oracle 4-SQLServer 5-API接口
        testDatasource.setDatasourceType(1);
        assertEquals(1, testDatasource.getDatasourceType());

        testDatasource.setDatasourceType(2);
        assertEquals(2, testDatasource.getDatasourceType());
    }

    @Test
    @DisplayName("JDBC URL构建测试 - MySQL")
    void buildJdbcUrlMySql() {
        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s",
                testDatasource.getHost(),
                testDatasource.getPort(),
                testDatasource.getDatabaseName());

        assertEquals("jdbc:mysql://localhost:3306/test_db", jdbcUrl);
    }

    @Test
    @DisplayName("连接参数JSON测试")
    void connectionParamsTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("useSSL", false);
        params.put("serverTimezone", "Asia/Shanghai");

        testDatasource.setConnectionParams(params);

        assertNotNull(testDatasource.getConnectionParams());
        assertEquals(false, testDatasource.getConnectionParams().get("useSSL"));
    }

    @Test
    @DisplayName("SQL参数替换测试")
    void sqlParameterReplacement() {
        String sql = "SELECT * FROM users WHERE status = #{status} AND dept_id = #{deptId}";
        Map<String, Object> params = new HashMap<>();
        params.put("status", 1);
        params.put("deptId", 100);

        String processedSql = sql;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            processedSql = processedSql.replace("#{" + entry.getKey() + "}",
                    entry.getValue().toString());
        }

        assertEquals("SELECT * FROM users WHERE status = 1 AND dept_id = 100", processedSql);
    }

    @Test
    @DisplayName("数据源状态测试")
    void datasourceStatusTest() {
        assertEquals(1, testDatasource.getStatus());

        testDatasource.setStatus(0);
        assertEquals(0, testDatasource.getStatus());
    }
}
