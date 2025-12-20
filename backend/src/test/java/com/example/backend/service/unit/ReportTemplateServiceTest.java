package com.example.backend.service.unit;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.ReportTemplateDTO;
import com.example.backend.dto.ReportTemplateQueryDTO;
import com.example.backend.entity.ReportTemplate;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReportTemplateMapper;
import com.example.backend.service.impl.ReportTemplateServiceImpl;
import com.example.backend.vo.ReportTemplateVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 报表模板服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReportTemplateServiceTest {

    @Mock
    private ReportTemplateMapper reportTemplateMapper;

    @InjectMocks
    private ReportTemplateServiceImpl reportTemplateService;

    private ReportTemplate testTemplate;

    @BeforeEach
    void setUp() {
        testTemplate = new ReportTemplate();
        testTemplate.setId(1L);
        testTemplate.setTemplateName("测试模板");
        testTemplate.setTemplateCode("TPL_TEST_001");
        testTemplate.setTemplateType(1);
        testTemplate.setStatus(0); // 草稿
        testTemplate.setVersion(1);
    }

    @Test
    @DisplayName("创建模板成功")
    void createTemplate_Success() {
        // Given
        ReportTemplateDTO dto = new ReportTemplateDTO();
        dto.setTemplateName("新模板");
        dto.setTemplateCode("TPL_NEW_001");
        dto.setTemplateType(1);

        when(reportTemplateMapper.insert(any(ReportTemplate.class))).thenReturn(1);

        // When
        Long templateId = reportTemplateService.createTemplate(dto);

        // Then
        verify(reportTemplateMapper).insert(any(ReportTemplate.class));
    }

    @Test
    @DisplayName("获取模板详情成功")
    void getTemplateDetail_Success() {
        // Given
        when(reportTemplateMapper.selectById(1L)).thenReturn(testTemplate);

        // When
        ReportTemplateVO vo = reportTemplateService.getTemplateDetail(1L);

        // Then
        assertNotNull(vo);
        assertEquals("测试模板", vo.getTemplateName());
    }

    @Test
    @DisplayName("获取模板详情失败 - 模板不存在")
    void getTemplateDetail_NotFound() {
        // Given
        when(reportTemplateMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reportTemplateService.getTemplateDetail(999L));
        assertEquals(ResultCode.TEMPLATE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("发布模板成功")
    void publishTemplate_Success() {
        // Given
        testTemplate.setStatus(0); // 草稿状态
        when(reportTemplateMapper.selectById(1L)).thenReturn(testTemplate);
        when(reportTemplateMapper.updateById(any(ReportTemplate.class))).thenReturn(1);

        // When
        reportTemplateService.publishTemplate(1L);

        // Then
        verify(reportTemplateMapper).updateById(argThat(template ->
                ((ReportTemplate) template).getStatus() == 1));
    }

    @Test
    @DisplayName("下线模板成功")
    void offlineTemplate_Success() {
        // Given
        testTemplate.setStatus(1); // 已发布状态
        when(reportTemplateMapper.selectById(1L)).thenReturn(testTemplate);
        when(reportTemplateMapper.updateById(any(ReportTemplate.class))).thenReturn(1);

        // When
        reportTemplateService.offlineTemplate(1L);

        // Then
        verify(reportTemplateMapper).updateById(argThat(template ->
                ((ReportTemplate) template).getStatus() == 2));
    }

    @Test
    @DisplayName("删除模板成功")
    void deleteTemplate_Success() {
        // Given
        when(reportTemplateMapper.selectById(1L)).thenReturn(testTemplate);
        when(reportTemplateMapper.deleteById(1L)).thenReturn(1);

        // When
        reportTemplateService.deleteTemplate(1L);

        // Then
        verify(reportTemplateMapper).deleteById(1L);
    }

    @Test
    @DisplayName("复制模板成功")
    void copyTemplate_Success() {
        // Given
        when(reportTemplateMapper.selectById(1L)).thenReturn(testTemplate);
        when(reportTemplateMapper.insert(any(ReportTemplate.class))).thenReturn(1);

        // When
        Long newId = reportTemplateService.copyTemplate(1L, "复制的模板");

        // Then
        verify(reportTemplateMapper).insert(argThat(template ->
                "复制的模板".equals(((ReportTemplate) template).getTemplateName())));
    }

    @Test
    @DisplayName("更新模板成功")
    void updateTemplate_Success() {
        // Given
        ReportTemplateDTO dto = new ReportTemplateDTO();
        dto.setTemplateName("更新后的模板");
        dto.setDescription("更新描述");

        when(reportTemplateMapper.selectById(1L)).thenReturn(testTemplate);
        when(reportTemplateMapper.updateById(any(ReportTemplate.class))).thenReturn(1);

        // When
        reportTemplateService.updateTemplate(1L, dto);

        // Then
        verify(reportTemplateMapper).updateById(any(ReportTemplate.class));
    }
}
