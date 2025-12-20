package com.example.backend.service.unit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.ReportScheduleDTO;
import com.example.backend.dto.ReportScheduleQueryDTO;
import com.example.backend.entity.ReportSchedule;
import com.example.backend.entity.ReportTemplate;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReportScheduleMapper;
import com.example.backend.mapper.ReportTemplateMapper;
import com.example.backend.service.impl.ReportScheduleServiceImpl;
import com.example.backend.vo.ReportScheduleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 定时报表任务服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReportScheduleServiceTest {

    @Mock
    private ReportScheduleMapper reportScheduleMapper;

    @Mock
    private ReportTemplateMapper reportTemplateMapper;

    @InjectMocks
    private ReportScheduleServiceImpl reportScheduleService;

    private ReportSchedule testSchedule;
    private ReportTemplate testTemplate;
    private ReportScheduleDTO testScheduleDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试模板
        testTemplate = new ReportTemplate();
        testTemplate.setId(1L);
        testTemplate.setTemplateName("日报模板");
        testTemplate.setTemplateCode("TPL_DAILY_001");
        testTemplate.setStatus(1); // 已发布

        // 初始化测试定时任务
        testSchedule = new ReportSchedule();
        testSchedule.setId(1L);
        testSchedule.setScheduleName("每日销售报表");
        testSchedule.setTemplateId(1L);
        testSchedule.setCronExpression("0 0 8 * * ?"); // 每天8点
        testSchedule.setExportFormat("xlsx");
        testSchedule.setStatus(1); // 启用
        testSchedule.setRecipients("admin@company.com,manager@company.com");
        testSchedule.setCreateTime(LocalDateTime.now());

        // 初始化创建DTO
        testScheduleDTO = new ReportScheduleDTO();
        testScheduleDTO.setScheduleName("新定时任务");
        testScheduleDTO.setTemplateId(1L);
        testScheduleDTO.setCronExpression("0 0 9 * * ?");
        testScheduleDTO.setExportFormat("xlsx");
        testScheduleDTO.setRecipients(Arrays.asList("user1@company.com"));
    }

    @Test
    @DisplayName("创建定时任务成功")
    void createSchedule_Success() {
        // Given
        when(reportTemplateMapper.selectById(1L)).thenReturn(testTemplate);
        when(reportScheduleMapper.insert(any(ReportSchedule.class))).thenReturn(1);

        // When
        Long scheduleId = reportScheduleService.createSchedule(testScheduleDTO);

        // Then
        verify(reportScheduleMapper).insert(any(ReportSchedule.class));
    }

    @Test
    @DisplayName("创建定时任务失败 - 模板不存在")
    void createSchedule_TemplateNotFound() {
        // Given
        when(reportTemplateMapper.selectById(anyLong())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reportScheduleService.createSchedule(testScheduleDTO));
        assertEquals(ResultCode.TEMPLATE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("创建定时任务失败 - Cron表达式无效")
    void createSchedule_InvalidCronExpression() {
        // Given
        testScheduleDTO.setCronExpression("invalid-cron");
        when(reportTemplateMapper.selectById(1L)).thenReturn(testTemplate);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reportScheduleService.createSchedule(testScheduleDTO));
        assertEquals(ResultCode.INVALID_CRON_EXPRESSION.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("获取任务详情成功")
    void getScheduleDetail_Success() {
        // Given
        when(reportScheduleMapper.selectById(1L)).thenReturn(testSchedule);

        // When
        ReportScheduleVO vo = reportScheduleService.getScheduleDetail(1L);

        // Then
        assertNotNull(vo);
        assertEquals("每日销售报表", vo.getScheduleName());
        assertEquals("0 0 8 * * ?", vo.getCronExpression());
    }

    @Test
    @DisplayName("获取任务详情失败 - 任务不存在")
    void getScheduleDetail_NotFound() {
        // Given
        when(reportScheduleMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reportScheduleService.getScheduleDetail(999L));
        assertEquals(ResultCode.SCHEDULE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("启用任务成功")
    void enableSchedule_Success() {
        // Given
        testSchedule.setStatus(0); // 当前禁用
        when(reportScheduleMapper.selectById(1L)).thenReturn(testSchedule);
        when(reportScheduleMapper.updateById(any())).thenReturn(1);

        // When
        assertDoesNotThrow(() -> reportScheduleService.enableSchedule(1L));

        // Then
        verify(reportScheduleMapper).updateById(any());
    }

    @Test
    @DisplayName("禁用任务成功")
    void disableSchedule_Success() {
        // Given
        testSchedule.setStatus(1); // 当前启用
        when(reportScheduleMapper.selectById(1L)).thenReturn(testSchedule);
        when(reportScheduleMapper.updateById(any())).thenReturn(1);

        // When
        assertDoesNotThrow(() -> reportScheduleService.disableSchedule(1L));

        // Then
        verify(reportScheduleMapper).updateById(any());
    }

    @Test
    @DisplayName("删除任务成功")
    void deleteSchedule_Success() {
        // Given
        when(reportScheduleMapper.selectById(1L)).thenReturn(testSchedule);
        when(reportScheduleMapper.deleteById(1L)).thenReturn(1);

        // When
        assertDoesNotThrow(() -> reportScheduleService.deleteSchedule(1L));

        // Then
        verify(reportScheduleMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除任务失败 - 任务不存在")
    void deleteSchedule_NotFound() {
        // Given
        when(reportScheduleMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reportScheduleService.deleteSchedule(999L));
        assertEquals(ResultCode.SCHEDULE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("分页查询任务")
    void pageSchedules_Success() {
        // Given
        ReportScheduleQueryDTO queryDTO = new ReportScheduleQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        Page<ReportSchedule> page = new Page<>(1, 10);
        page.setRecords(List.of(testSchedule));
        page.setTotal(1);

        when(reportScheduleMapper.selectPage(any(), any())).thenReturn(page);

        // When
        IPage<ReportScheduleVO> result = reportScheduleService.pageSchedules(queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("更新任务成功")
    void updateSchedule_Success() {
        // Given
        when(reportScheduleMapper.selectById(1L)).thenReturn(testSchedule);
        when(reportTemplateMapper.selectById(1L)).thenReturn(testTemplate);
        when(reportScheduleMapper.updateById(any())).thenReturn(1);

        // When
        assertDoesNotThrow(() -> reportScheduleService.updateSchedule(1L, testScheduleDTO));

        // Then
        verify(reportScheduleMapper).updateById(any());
    }

    @Test
    @DisplayName("立即执行任务")
    void executeNow_Success() {
        // Given
        when(reportScheduleMapper.selectById(1L)).thenReturn(testSchedule);

        // When
        assertDoesNotThrow(() -> reportScheduleService.executeNow(1L));
    }

    @Test
    @DisplayName("立即执行任务失败 - 任务已禁用")
    void executeNow_ScheduleDisabled() {
        // Given
        testSchedule.setStatus(0); // 禁用状态
        when(reportScheduleMapper.selectById(1L)).thenReturn(testSchedule);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reportScheduleService.executeNow(1L));
        assertEquals(ResultCode.SCHEDULE_DISABLED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("Cron表达式格式验证")
    void validateCronExpression() {
        // 有效的Cron表达式
        assertTrue(isValidCron("0 0 8 * * ?"));      // 每天8点
        assertTrue(isValidCron("0 0/30 * * * ?"));   // 每30分钟
        assertTrue(isValidCron("0 0 9 ? * MON-FRI")); // 工作日9点

        // 无效的Cron表达式
        assertFalse(isValidCron("invalid"));
        assertFalse(isValidCron("0 0 25 * * ?")); // 无效小时
    }

    private boolean isValidCron(String cron) {
        try {
            // 简单验证：检查是否有6个或7个字段
            String[] parts = cron.split("\\s+");
            return parts.length >= 6 && parts.length <= 7;
        } catch (Exception e) {
            return false;
        }
    }
}
