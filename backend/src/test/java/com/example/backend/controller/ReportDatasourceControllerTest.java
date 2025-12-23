package com.example.backend.controller;

import com.example.backend.dto.ReportDatasourceDTO;
import com.example.backend.service.ReportDatasourceService;
import com.example.backend.vo.ReportDatasourceVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 数据源控制器集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportDatasourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportDatasourceService reportDatasourceService;

    @Test
    @DisplayName("创建MySQL数据源 - 成功")
    void createMysqlDatasource_Success() throws Exception {
        // Given
        ReportDatasourceDTO dto = new ReportDatasourceDTO();
        dto.setDatasourceName("生产MySQL");
        dto.setDatasourceCode("DS_MYSQL_PROD");
        dto.setDatasourceType(1); // 1-MySQL
        dto.setHost("localhost");
        dto.setPort(3306);
        dto.setDatabaseName("report_db");
        dto.setUsername("root");
        dto.setPassword("password123");

        when(reportDatasourceService.createDatasource(any(ReportDatasourceDTO.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/report/datasource")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("创建PostgreSQL数据源 - 成功")
    void createPostgresDatasource_Success() throws Exception {
        // Given
        ReportDatasourceDTO dto = new ReportDatasourceDTO();
        dto.setDatasourceName("分析PostgreSQL");
        dto.setDatasourceCode("DS_PG_ANALYSIS");
        dto.setDatasourceType(2); // 2-PostgreSQL
        dto.setHost("localhost");
        dto.setPort(5432);
        dto.setDatabaseName("analytics");
        dto.setUsername("postgres");
        dto.setPassword("password123");

        when(reportDatasourceService.createDatasource(any(ReportDatasourceDTO.class))).thenReturn(2L);

        // When & Then
        mockMvc.perform(post("/report/datasource")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("创建API数据源 - 成功")
    void createApiDatasource_Success() throws Exception {
        // Given
        ReportDatasourceDTO dto = new ReportDatasourceDTO();
        dto.setDatasourceName("外部API数据源");
        dto.setDatasourceCode("DS_API_EXT");
        dto.setDatasourceType(5); // 5-API接口
        dto.setApiUrl("https://api.example.com/data");
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        dto.setApiHeaders(headers);

        when(reportDatasourceService.createDatasource(any(ReportDatasourceDTO.class))).thenReturn(3L);

        // When & Then
        mockMvc.perform(post("/report/datasource")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取数据源详情 - 成功")
    void getDatasourceDetail_Success() throws Exception {
        // Given
        ReportDatasourceVO vo = new ReportDatasourceVO();
        vo.setId(1L);
        vo.setDatasourceName("生产MySQL");
        vo.setDatasourceCode("DS_MYSQL_PROD");
        vo.setDatasourceType(1);
        vo.setStatus(1);
        vo.setCreateTime(LocalDateTime.now());

        when(reportDatasourceService.getDatasourceDetail(1L)).thenReturn(vo);

        // When & Then
        mockMvc.perform(get("/report/datasource/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.datasourceName").value("生产MySQL"));
    }

    @Test
    @DisplayName("测试数据源连接 - 成功")
    void testConnection_Success() throws Exception {
        // Given
        when(reportDatasourceService.testConnection(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/report/datasource/1/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("测试数据源连接 - 失败")
    void testConnection_Failed() throws Exception {
        // Given
        when(reportDatasourceService.testConnection(1L)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/report/datasource/1/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("获取数据源列表 - 成功")
    void listDatasources_Success() throws Exception {
        // Given
        ReportDatasourceVO vo1 = new ReportDatasourceVO();
        vo1.setId(1L);
        vo1.setDatasourceName("MySQL数据源");

        ReportDatasourceVO vo2 = new ReportDatasourceVO();
        vo2.setId(2L);
        vo2.setDatasourceName("PostgreSQL数据源");

        when(reportDatasourceService.listAllDatasources()).thenReturn(Arrays.asList(vo1, vo2));

        // When & Then
        mockMvc.perform(get("/report/datasource/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("执行SQL查询 - 成功")
    void executeQuery_Success() throws Exception {
        // Given
        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", 1);
        row1.put("name", "Product A");

        when(reportDatasourceService.executeQuery(anyLong(), any(), any())).thenReturn(Arrays.asList(row1));

        // When & Then
        mockMvc.perform(post("/report/datasource/1/query")
                        .param("sql", "SELECT * FROM products LIMIT 10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除数据源 - 成功")
    void deleteDatasource_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/report/datasource/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("更新数据源 - 成功")
    void updateDatasource_Success() throws Exception {
        // Given
        ReportDatasourceDTO dto = new ReportDatasourceDTO();
        dto.setDatasourceName("生产MySQL-更新");
        dto.setDatasourceCode("DS_MYSQL_PROD");
        dto.setDatasourceType(1);
        dto.setHost("192.168.1.100");
        dto.setPort(3306);
        dto.setDatabaseName("report_db");
        dto.setUsername("root");
        dto.setPassword("newpassword");

        // When & Then
        mockMvc.perform(put("/report/datasource/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
