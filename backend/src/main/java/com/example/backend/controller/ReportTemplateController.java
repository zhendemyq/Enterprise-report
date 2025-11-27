package com.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.dto.ReportTemplateDTO;
import com.example.backend.dto.ReportTemplateQueryDTO;
import com.example.backend.service.ReportTemplateService;
import com.example.backend.vo.ReportTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报表模板控制器
 */
@Tag(name = "报表模板管理", description = "模板CRUD、设计、发布等接口")
@RestController
@RequestMapping("/report/template")
public class ReportTemplateController {

    @Autowired
    private ReportTemplateService templateService;

    @Operation(summary = "创建模板")
    @PostMapping
    public Result<Long> createTemplate(@Valid @RequestBody ReportTemplateDTO templateDTO) {
        return Result.success(templateService.createTemplate(templateDTO));
    }

    @Operation(summary = "更新模板")
    @PutMapping("/{id}")
    public Result<Void> updateTemplate(@PathVariable Long id, @Valid @RequestBody ReportTemplateDTO templateDTO) {
        templateService.updateTemplate(id, templateDTO);
        return Result.success();
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return Result.success();
    }

    @Operation(summary = "获取模板详情")
    @GetMapping("/{id}")
    public Result<ReportTemplateVO> getTemplateDetail(@PathVariable Long id) {
        return Result.success(templateService.getTemplateDetail(id));
    }

    @Operation(summary = "分页查询模板")
    @GetMapping("/page")
    public Result<PageResult<ReportTemplateVO>> pageTemplates(ReportTemplateQueryDTO queryDTO) {
        IPage<ReportTemplateVO> page = templateService.pageTemplates(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "根据分类获取模板列表")
    @GetMapping("/category/{categoryId}")
    public Result<List<ReportTemplateVO>> listByCategory(@PathVariable Long categoryId) {
        return Result.success(templateService.listByCategory(categoryId));
    }

    @Operation(summary = "发布模板")
    @PutMapping("/{id}/publish")
    public Result<Void> publishTemplate(@PathVariable Long id) {
        templateService.publishTemplate(id);
        return Result.success();
    }

    @Operation(summary = "下线模板")
    @PutMapping("/{id}/offline")
    public Result<Void> offlineTemplate(@PathVariable Long id) {
        templateService.offlineTemplate(id);
        return Result.success();
    }

    @Operation(summary = "复制模板")
    @PostMapping("/{id}/copy")
    public Result<Long> copyTemplate(@PathVariable Long id, @RequestParam String newName) {
        return Result.success(templateService.copyTemplate(id, newName));
    }

    @Operation(summary = "保存模板设计")
    @PutMapping("/{id}/design")
    public Result<Void> saveTemplateDesign(@PathVariable Long id, @RequestBody String designJson) {
        templateService.saveTemplateDesign(id, designJson);
        return Result.success();
    }

    @Operation(summary = "获取用户有权限的模板列表")
    @GetMapping("/user")
    public Result<List<ReportTemplateVO>> listUserTemplates() {
        return Result.success(templateService.listUserTemplates());
    }
}
