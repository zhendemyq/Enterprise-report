package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.ReportCategoryDTO;
import com.example.backend.entity.ReportCategory;
import com.example.backend.vo.ReportCategoryVO;

import java.util.List;

/**
 * 报表分类服务接口
 */
public interface ReportCategoryService extends IService<ReportCategory> {

    /**
     * 创建分类
     */
    Long createCategory(ReportCategoryDTO categoryDTO);

    /**
     * 更新分类
     */
    void updateCategory(Long id, ReportCategoryDTO categoryDTO);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);

    /**
     * 获取分类树
     */
    List<ReportCategoryVO> getCategoryTree();

    /**
     * 获取分类详情
     */
    ReportCategoryVO getCategoryDetail(Long id);
}
