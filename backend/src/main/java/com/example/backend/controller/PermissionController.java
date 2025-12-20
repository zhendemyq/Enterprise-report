package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.common.Result;
import com.example.backend.entity.ReportCategory;
import com.example.backend.entity.ReportTemplate;
import com.example.backend.mapper.ReportCategoryMapper;
import com.example.backend.mapper.ReportTemplateMapper;
import com.example.backend.vo.PermissionTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限控制器
 */
@Tag(name = "权限管理", description = "权限树接口")
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private ReportCategoryMapper reportCategoryMapper;

    @Autowired
    private ReportTemplateMapper reportTemplateMapper;

    @Operation(summary = "获取权限树")
    @GetMapping("/tree")
    public Result<List<PermissionTreeVO>> getPermissionTree() {
        // 获取所有分类
        LambdaQueryWrapper<ReportCategory> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.orderByAsc(ReportCategory::getSort);
        List<ReportCategory> categories = reportCategoryMapper.selectList(categoryWrapper);

        // 获取所有模板
        LambdaQueryWrapper<ReportTemplate> templateWrapper = new LambdaQueryWrapper<>();
        templateWrapper.eq(ReportTemplate::getStatus, 1)  // 只显示已发布的模板
                .orderByAsc(ReportTemplate::getSort);
        List<ReportTemplate> templates = reportTemplateMapper.selectList(templateWrapper);

        // 按分类ID分组模板
        Map<Long, List<ReportTemplate>> templateMap = templates.stream()
                .filter(t -> t.getCategoryId() != null)
                .collect(Collectors.groupingBy(ReportTemplate::getCategoryId));

        // 构建树形结构
        List<PermissionTreeVO> tree = new ArrayList<>();
        
        // 先添加分类节点
        for (ReportCategory category : categories) {
            if (category.getParentId() == null || category.getParentId() == 0L) {
                PermissionTreeVO categoryNode = buildCategoryNode(category, categories, templateMap);
                tree.add(categoryNode);
            }
        }

        // 添加未分类的模板
        List<ReportTemplate> uncategorizedTemplates = templates.stream()
                .filter(t -> t.getCategoryId() == null)
                .collect(Collectors.toList());
        if (!uncategorizedTemplates.isEmpty()) {
            PermissionTreeVO uncategorizedNode = new PermissionTreeVO();
            uncategorizedNode.setId(-1L);  // 特殊ID表示未分类
            uncategorizedNode.setLabel("未分类");
            uncategorizedNode.setType("category");
            uncategorizedNode.setChildren(uncategorizedTemplates.stream()
                    .map(this::buildTemplateNode)
                    .collect(Collectors.toList()));
            tree.add(uncategorizedNode);
        }

        return Result.success(tree);
    }

    /**
     * 构建分类节点
     */
    private PermissionTreeVO buildCategoryNode(ReportCategory category, 
                                                List<ReportCategory> allCategories,
                                                Map<Long, List<ReportTemplate>> templateMap) {
        PermissionTreeVO node = new PermissionTreeVO();
        node.setId(category.getId() * -1);  // 分类ID取负值，避免与模板ID冲突
        node.setLabel(category.getCategoryName());
        node.setParentId(category.getParentId());
        node.setType("category");

        List<PermissionTreeVO> children = new ArrayList<>();

        // 添加子分类
        for (ReportCategory child : allCategories) {
            if (category.getId().equals(child.getParentId())) {
                children.add(buildCategoryNode(child, allCategories, templateMap));
            }
        }

        // 添加该分类下的模板
        List<ReportTemplate> categoryTemplates = templateMap.get(category.getId());
        if (categoryTemplates != null) {
            for (ReportTemplate template : categoryTemplates) {
                children.add(buildTemplateNode(template));
            }
        }

        node.setChildren(children);
        return node;
    }

    /**
     * 构建模板节点
     */
    private PermissionTreeVO buildTemplateNode(ReportTemplate template) {
        PermissionTreeVO node = new PermissionTreeVO();
        node.setId(template.getId());  // 模板ID保持正值
        node.setLabel(template.getTemplateName());
        node.setType("template");
        node.setChildren(new ArrayList<>());
        return node;
    }
}
