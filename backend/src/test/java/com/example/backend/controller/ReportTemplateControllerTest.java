package com.example.backend.controller;

import com.example.backend.dto.ReportTemplateDTO;
import com.example.backend.service.ReportTemplateService;
import com.example.backend.vo.ReportTemplateVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 报表模板控制器集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReportTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportTemplateService reportTemplateService;

    @Test
    @DisplayName("创建模板 - 成功")
    void createTemplate_Success() throws Exception {
        // Given
        ReportTemplateDTO dto = new ReportTemplateDTO();
        dto.setTemplateName("销售日报");
        dto.setTemplateCode("TPL_SALES_001");
        dto.setTemplateType(1);
        dto.setCategoryId(1L);

        when(reportTemplateService.createTemplate(any(ReportTemplateDTO.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/report/template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("创建模板 - 参数校验失败（模板名称为空）")
    void createTemplate_ValidationFailed_EmptyName() throws Exception {
        // Given
        ReportTemplateDTO dto = new ReportTemplateDTO();
        dto.setTemplateName(""); // 空名称
        dto.setTemplateCode("TPL_TEST_001");
        dto.setTemplateType(1);

        // When & Then
        mockMvc.perform(post("/report/template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取模板详情 - 成功")
    void getTemplateDetail_Success() throws Exception {
        // Given
        ReportTemplateVO vo = new ReportTemplateVO();
        vo.setId(1L);
        vo.setTemplateName("销售日报");
        vo.setTemplateCode("TPL_SALES_001");
        vo.setTemplateType(1);
        vo.setStatus(0);
        vo.setVersion(1);
        vo.setCreateTime(LocalDateTime.now());

        when(reportTemplateService.getTemplateDetail(1L)).thenReturn(vo);

        // When & Then
        mockMvc.perform(get("/report/template/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.templateName").value("销售日报"));
    }

    @Test
    @DisplayName("更新模板 - 成功")
    void updateTemplate_Success() throws Exception {
        // Given
        ReportTemplateDTO dto = new ReportTemplateDTO();
        dto.setTemplateName("销售日报V2");
        dto.setTemplateCode("TPL_SALES_001");
        dto.setTemplateType(1);

        // When & Then
        mockMvc.perform(put("/report/template/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除模板 - 成功")
    void deleteTemplate_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/report/template/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("发布模板 - 成功")
    void publishTemplate_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/report/template/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("下线模板 - 成功")
    void offlineTemplate_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/report/template/1/offline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("复制模板 - 成功")
    void copyTemplate_Success() throws Exception {
        // Given
        when(reportTemplateService.copyTemplate(anyLong())).thenReturn(2L);

        // When & Then
        mockMvc.perform(post("/report/template/1/copy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(2));
    }
}
