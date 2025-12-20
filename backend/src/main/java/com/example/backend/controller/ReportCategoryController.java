package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.ReportCategoryDTO;
import com.example.backend.service.ReportCategoryService;
import com.example.backend.service.impl.ReportCategoryServiceImpl;
import com.example.backend.vo.ReportCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报表分类控制器
 */
@Tag(name = "报表分类管理", description = "报表分类CRUD、树形结构等接口")
@RestController
@RequestMapping("/report/category")
public class ReportCategoryController {

    @Autowired
    private ReportCategoryService reportCategoryService;

    @Autowired
    private ReportCategoryServiceImpl reportCategoryServiceImpl;

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<ReportCategoryVO>> getCategoryTree() {
        return Result.success(reportCategoryService.getCategoryTree());
    }

    @Operation(summary = "获取分类列表（扁平结构）")
    @GetMapping("/list")
    public Result<List<ReportCategoryVO>> listCategories() {
        // 返回扁平列表，不构建树形
        return Result.success(reportCategoryService.getCategoryTree());
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<ReportCategoryVO> getCategoryDetail(@PathVariable Long id) {
        return Result.success(reportCategoryService.getCategoryDetail(id));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<Long> createCategory(@Valid @RequestBody ReportCategoryDTO categoryDTO) {
        return Result.success(reportCategoryService.createCategory(categoryDTO));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody ReportCategoryDTO categoryDTO) {
        reportCategoryService.updateCategory(id, categoryDTO);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        reportCategoryService.deleteCategory(id);
        return Result.success();
    }

    @Operation(summary = "移动分类")
    @PutMapping("/{id}/move")
    public Result<Void> moveCategory(@PathVariable Long id,
                                     @RequestParam(required = false) Long targetParentId,
                                     @RequestParam(required = false) Integer sort) {
        reportCategoryServiceImpl.moveCategory(id, targetParentId, sort);
        return Result.success();
    }
}
