package com.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.dto.ReportCategoryDTO;
import com.example.backend.entity.ReportCategory;
import com.example.backend.entity.ReportTemplate;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReportCategoryMapper;
import com.example.backend.mapper.ReportTemplateMapper;
import com.example.backend.service.ReportCategoryService;
import com.example.backend.vo.ReportCategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表分类服务实现
 */
@Service
public class ReportCategoryServiceImpl extends ServiceImpl<ReportCategoryMapper, ReportCategory> implements ReportCategoryService {

    @Autowired
    private ReportTemplateMapper reportTemplateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(ReportCategoryDTO categoryDTO) {
        // 检查分类编码是否存在
        LambdaQueryWrapper<ReportCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportCategory::getCategoryCode, categoryDTO.getCategoryCode());
        if (count(wrapper) > 0) {
            throw new BusinessException("分类编码已存在");
        }

        ReportCategory category = BeanUtil.copyProperties(categoryDTO, ReportCategory.class);
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        save(category);
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, ReportCategoryDTO categoryDTO) {
        ReportCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查分类编码是否被其他分类使用
        LambdaQueryWrapper<ReportCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportCategory::getCategoryCode, categoryDTO.getCategoryCode())
                .ne(ReportCategory::getId, id);
        if (count(wrapper) > 0) {
            throw new BusinessException("分类编码已存在");
        }

        BeanUtil.copyProperties(categoryDTO, category, "id");
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        ReportCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否有子分类
        LambdaQueryWrapper<ReportCategory> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(ReportCategory::getParentId, id);
        if (count(childWrapper) > 0) {
            throw new BusinessException("该分类下有子分类，无法删除");
        }

        // 检查是否有关联模板
        LambdaQueryWrapper<ReportTemplate> templateWrapper = new LambdaQueryWrapper<>();
        templateWrapper.eq(ReportTemplate::getCategoryId, id);
        if (reportTemplateMapper.selectCount(templateWrapper) > 0) {
            throw new BusinessException("该分类下有关联模板，无法删除");
        }

        removeById(id);
    }

    @Override
    public List<ReportCategoryVO> getCategoryTree() {
        // 查询所有分类
        LambdaQueryWrapper<ReportCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ReportCategory::getSort)
                .orderByAsc(ReportCategory::getId);
        List<ReportCategory> categories = list(wrapper);

        // 统计每个分类下的模板数量
        Map<Long, Long> templateCountMap = countTemplatesByCategory();

        // 转换为VO
        List<ReportCategoryVO> voList = categories.stream()
                .map(category -> {
                    ReportCategoryVO vo = BeanUtil.copyProperties(category, ReportCategoryVO.class);
                    vo.setStatusName(category.getStatus() == 1 ? "正常" : "禁用");
                    vo.setTemplateCount(templateCountMap.getOrDefault(category.getId(), 0L).intValue());
                    return vo;
                })
                .collect(Collectors.toList());

        // 构建树形结构
        return buildTree(voList, 0L);
    }

    @Override
    public ReportCategoryVO getCategoryDetail(Long id) {
        ReportCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        ReportCategoryVO vo = BeanUtil.copyProperties(category, ReportCategoryVO.class);
        vo.setStatusName(category.getStatus() == 1 ? "正常" : "禁用");
        return vo;
    }

    /**
     * 统计每个分类下的模板数量
     */
    private Map<Long, Long> countTemplatesByCategory() {
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(ReportTemplate::getCategoryId);
        List<ReportTemplate> templates = reportTemplateMapper.selectList(wrapper);
        return templates.stream()
                .filter(t -> t.getCategoryId() != null)
                .collect(Collectors.groupingBy(ReportTemplate::getCategoryId, Collectors.counting()));
    }

    /**
     * 构建树形结构
     */
    private List<ReportCategoryVO> buildTree(List<ReportCategoryVO> list, Long parentId) {
        List<ReportCategoryVO> tree = new ArrayList<>();
        for (ReportCategoryVO vo : list) {
            if (parentId.equals(vo.getParentId())) {
                vo.setChildren(buildTree(list, vo.getId()));
                tree.add(vo);
            }
        }
        return tree;
    }

    /**
     * 移动分类
     */
    public void moveCategory(Long id, Long targetParentId, Integer sort) {
        ReportCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 不能将分类移动到自己的子分类下
        if (isChild(id, targetParentId)) {
            throw new BusinessException("不能将分类移动到自己的子分类下");
        }

        category.setParentId(targetParentId != null ? targetParentId : 0L);
        if (sort != null) {
            category.setSort(sort);
        }
        updateById(category);
    }

    /**
     * 检查targetId是否是id的子分类
     */
    private boolean isChild(Long id, Long targetId) {
        if (targetId == null || targetId == 0L) {
            return false;
        }
        if (id.equals(targetId)) {
            return true;
        }
        LambdaQueryWrapper<ReportCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportCategory::getParentId, id);
        List<ReportCategory> children = list(wrapper);
        for (ReportCategory child : children) {
            if (isChild(child.getId(), targetId)) {
                return true;
            }
        }
        return false;
    }
}
