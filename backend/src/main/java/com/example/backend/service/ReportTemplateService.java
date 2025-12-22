package com.example.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.ReportTemplateDTO;
import com.example.backend.dto.ReportTemplateQueryDTO;
import com.example.backend.entity.ReportTemplate;
import com.example.backend.vo.ReportTemplateVO;

import java.util.List;

/**
 * 报表模板服务接口
 */
public interface ReportTemplateService extends IService<ReportTemplate> {

    /**
     * 创建模板
     */
    Long createTemplate(ReportTemplateDTO templateDTO);

    /**
     * 更新模板
     */
    void updateTemplate(Long id, ReportTemplateDTO templateDTO);

    /**
     * 删除模板
     */
    void deleteTemplate(Long id);

    /**
     * 获取模板详情
     */
    ReportTemplateVO getTemplateDetail(Long id);

    /**
     * 分页查询模板
     */
    IPage<ReportTemplateVO> pageTemplates(ReportTemplateQueryDTO queryDTO);

    /**
     * 根据分类获取模板列表
     */
    List<ReportTemplateVO> listByCategory(Long categoryId);

    /**
     * 发布模板
     */
    void publishTemplate(Long id);

    /**
     * 下线模板
     */
    void offlineTemplate(Long id);

    /**
     * 复制模板
     */
    Long copyTemplate(Long id, String newName);

    /**
     * 保存模板设计（Univer配置）
     */
    void saveTemplateDesign(Long id, String designJson);

    /**
     * 获取用户有权限的模板列表
     */
    List<ReportTemplateVO> listUserTemplates();

    /**
     * 增加模板使用次数
     */
    void incrementUseCount(Long id);
}
