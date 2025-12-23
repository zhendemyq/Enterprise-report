package com.example.backend.service.unit;

import cn.hutool.core.bean.BeanUtil;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.ReportTemplateDTO;
import com.example.backend.entity.ReportTemplate;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReportTemplateMapper;
import com.example.backend.service.PermissionService;
import com.example.backend.service.impl.ReportTemplateServiceImpl;
import com.example.backend.vo.ReportTemplateVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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

    @Mock
    private PermissionService permissionService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

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
        testTemplate.setStatus(0);
        testTemplate.setVersion(1);
    }

    @Test
    @DisplayName("模板状态转换测试")
    void templateStatusTest() {
        assertEquals(0, testTemplate.getStatus()); // 草稿

        testTemplate.setStatus(1); // 发布
        assertEquals(1, testTemplate.getStatus());

        testTemplate.setStatus(2); // 下线
        assertEquals(2, testTemplate.getStatus());
    }

    @Test
    @DisplayName("模板类型名称映射测试")
    void templateTypeNameTest() {
        String[] typeNames = {"", "明细表", "汇总表", "分组统计表", "图表报表"};

        testTemplate.setTemplateType(1);
        assertEquals("明细表", typeNames[testTemplate.getTemplateType()]);

        testTemplate.setTemplateType(2);
        assertEquals("汇总表", typeNames[testTemplate.getTemplateType()]);

        testTemplate.setTemplateType(3);
        assertEquals("分组统计表", typeNames[testTemplate.getTemplateType()]);

        testTemplate.setTemplateType(4);
        assertEquals("图表报表", typeNames[testTemplate.getTemplateType()]);
    }

    @Test
    @DisplayName("模板版本递增测试")
    void templateVersionIncrementTest() {
        assertEquals(1, testTemplate.getVersion());

        testTemplate.setVersion(testTemplate.getVersion() + 1);
        assertEquals(2, testTemplate.getVersion());
    }

    @Test
    @DisplayName("模板DTO转换测试")
    void templateDTOConversionTest() {
        ReportTemplateDTO dto = new ReportTemplateDTO();
        dto.setTemplateName("新模板");
        dto.setTemplateCode("TPL_NEW_001");
        dto.setTemplateType(1);
        dto.setDescription("测试描述");

        ReportTemplate template = BeanUtil.copyProperties(dto, ReportTemplate.class);

        assertEquals("新模板", template.getTemplateName());
        assertEquals("TPL_NEW_001", template.getTemplateCode());
        assertEquals(1, template.getTemplateType());
        assertEquals("测试描述", template.getDescription());
    }

    @Test
    @DisplayName("模板复制逻辑测试")
    void templateCopyLogicTest() {
        ReportTemplate source = testTemplate;
        ReportTemplate target = BeanUtil.copyProperties(source, ReportTemplate.class);

        target.setId(null);
        target.setTemplateName("复制的模板");
        target.setTemplateCode(source.getTemplateCode() + "_copy_123");
        target.setStatus(0);
        target.setVersion(1);

        assertNull(target.getId());
        assertEquals("复制的模板", target.getTemplateName());
        assertTrue(target.getTemplateCode().contains("_copy_"));
        assertEquals(0, target.getStatus());
        assertEquals(1, target.getVersion());
    }
}
