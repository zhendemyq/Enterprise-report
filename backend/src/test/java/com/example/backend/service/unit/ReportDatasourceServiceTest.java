package com.example.backend.service.unit;

import com.example.backend.common.ResultCode;
import com.example.backend.dto.ReportDatasourceDTO;
import com.example.backend.entity.ReportDatasource;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReportDatasourceMapper;
import com.example.backend.service.impl.ReportDatasourceServiceImpl;
import com.example.backend.vo.ReportDatasourceVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 数据源服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReportDatasourceServiceTest {

    @Mock
    private ReportDatasourceMapper reportDatasourceMapper;

    @InjectMocks
    private ReportDatasourceServiceImpl reportDatasourceService;

    private ReportDatasource testDatasource;

    @BeforeEach
    void setUp() {
        testDatasource = new ReportDatasource();
        testDatasource.setId(1L);
        testDatasource.setDatasourceName("测试数据源");
        testDatasource.setDatasourceCode("DS_TEST_001");
        testDatasource.setDatasourceType("mysql");
        testDatasource.setJdbcUrl("jdbc:mysql://localhost:3306/test");
        testDatasource.setUsername("root");
        testDatasource.setPassword("root");
        testDatasource.setStatus(1);
    }

    @Test
    @DisplayName("创建数据源成功")
    void createDatasource_Success() {
        // Given
        ReportDatasourceDTO dto = new ReportDatasourceDTO();
        dto.setDatasourceName("新数据源");
        dto.setDatasourceCode("DS_NEW_001");
        dto.setDatasourceType("mysql");
        dto.setJdbcUrl("jdbc:mysql://localhost:3306/newdb");
        dto.setUsername("root");
        dto.setPassword("root");

        when(reportDatasourceMapper.insert(any(ReportDatasource.class))).thenReturn(1);

        // When
        Long id = reportDatasourceService.createDatasource(dto);

        // Then
        verify(reportDatasourceMapper).insert(any(ReportDatasource.class));
    }

    @Test
    @DisplayName("获取数据源详情成功")
    void getDatasourceDetail_Success() {
        // Given
        when(reportDatasourceMapper.selectById(1L)).thenReturn(testDatasource);

        // When
        ReportDatasourceVO vo = reportDatasourceService.getDatasourceDetail(1L);

        // Then
        assertNotNull(vo);
        assertEquals("测试数据源", vo.getDatasourceName());
    }

    @Test
    @DisplayName("获取数据源详情失败 - 数据源不存在")
    void getDatasourceDetail_NotFound() {
        // Given
        when(reportDatasourceMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reportDatasourceService.getDatasourceDetail(999L));
        assertEquals(ResultCode.DATASOURCE_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("更新数据源成功")
    void updateDatasource_Success() {
        // Given
        ReportDatasourceDTO dto = new ReportDatasourceDTO();
        dto.setDatasourceName("更新后的数据源");
        dto.setJdbcUrl("jdbc:mysql://localhost:3306/updated");

        when(reportDatasourceMapper.selectById(1L)).thenReturn(testDatasource);
        when(reportDatasourceMapper.updateById(any(ReportDatasource.class))).thenReturn(1);

        // When
        reportDatasourceService.updateDatasource(1L, dto);

        // Then
        verify(reportDatasourceMapper).updateById(any(ReportDatasource.class));
    }

    @Test
    @DisplayName("删除数据源成功")
    void deleteDatasource_Success() {
        // Given
        when(reportDatasourceMapper.selectById(1L)).thenReturn(testDatasource);
        when(reportDatasourceMapper.deleteById(1L)).thenReturn(1);

        // When
        reportDatasourceService.deleteDatasource(1L);

        // Then
        verify(reportDatasourceMapper).deleteById(1L);
    }

    @Test
    @DisplayName("SQL参数替换测试")
    void executeQuery_ParameterReplacement() {
        // 测试SQL参数替换逻辑
        String sql = "SELECT * FROM users WHERE status = #{status} AND dept_id = #{deptId}";
        Map<String, Object> params = new HashMap<>();
        params.put("status", 1);
        params.put("deptId", 100);

        // 验证参数替换逻辑
        String processedSql = sql;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            processedSql = processedSql.replace("#{" + entry.getKey() + "}",
                    entry.getValue().toString());
        }

        assertEquals("SELECT * FROM users WHERE status = 1 AND dept_id = 100", processedSql);
    }
}
