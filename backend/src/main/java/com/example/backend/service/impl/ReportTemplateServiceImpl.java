package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.ReportTemplateDTO;
import com.example.backend.dto.ReportTemplateQueryDTO;
import com.example.backend.entity.ReportTemplate;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReportPermissionMapper;
import com.example.backend.mapper.ReportTemplateMapper;
import com.example.backend.service.ReportTemplateService;
import com.example.backend.vo.ReportTemplateVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表模板服务实现
 */
@Service
public class ReportTemplateServiceImpl extends ServiceImpl<ReportTemplateMapper, ReportTemplate> implements ReportTemplateService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReportPermissionMapper reportPermissionMapper;

    @Override
    public Long createTemplate(ReportTemplateDTO templateDTO) {
        // 检查模板编码是否重复
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportTemplate::getTemplateCode, templateDTO.getTemplateCode());
        if (count(wrapper) > 0) {
            throw new BusinessException("模板编码已存在");
        }

        ReportTemplate template = BeanUtil.copyProperties(templateDTO, ReportTemplate.class);
        template.setStatus(0); // 草稿状态
        template.setVersion(1);
        save(template);
        return template.getId();
    }

    @Override
    public void updateTemplate(Long id, ReportTemplateDTO templateDTO) {
        ReportTemplate template = getById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        BeanUtil.copyProperties(templateDTO, template, "id", "templateCode", "status", "version");
        updateById(template);
    }

    @Override
    public void deleteTemplate(Long id) {
        ReportTemplate template = getById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }
        removeById(id);
    }

    @Override
    public ReportTemplateVO getTemplateDetail(Long id) {
        ReportTemplate template = getById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }
        return convertToVO(template);
    }

    @Override
    public IPage<ReportTemplateVO> pageTemplates(ReportTemplateQueryDTO queryDTO) {
        Page<ReportTemplate> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(queryDTO.getTemplateName()), 
                    ReportTemplate::getTemplateName, queryDTO.getTemplateName())
                .like(StringUtils.isNotBlank(queryDTO.getTemplateCode()), 
                    ReportTemplate::getTemplateCode, queryDTO.getTemplateCode())
                .eq(queryDTO.getTemplateType() != null, ReportTemplate::getTemplateType, queryDTO.getTemplateType())
                .eq(queryDTO.getCategoryId() != null, ReportTemplate::getCategoryId, queryDTO.getCategoryId())
                .eq(queryDTO.getStatus() != null, ReportTemplate::getStatus, queryDTO.getStatus())
                .orderByAsc(ReportTemplate::getSort)
                .orderByDesc(ReportTemplate::getCreateTime);

        IPage<ReportTemplate> templatePage = page(page, wrapper);
        return templatePage.convert(this::convertToVO);
    }

    @Override
    public List<ReportTemplateVO> listByCategory(Long categoryId) {
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportTemplate::getCategoryId, categoryId)
                .eq(ReportTemplate::getStatus, 1)
                .orderByAsc(ReportTemplate::getSort);
        return list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void publishTemplate(Long id) {
        ReportTemplate template = getById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }
        template.setStatus(1);
        template.setVersion(template.getVersion() + 1);
        updateById(template);
    }

    @Override
    public void offlineTemplate(Long id) {
        ReportTemplate template = getById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }
        template.setStatus(2);
        updateById(template);
    }

    @Override
    public Long copyTemplate(Long id, String newName) {
        ReportTemplate source = getById(id);
        if (source == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        ReportTemplate target = BeanUtil.copyProperties(source, ReportTemplate.class);
        target.setId(null);
        target.setTemplateName(newName);
        target.setTemplateCode(source.getTemplateCode() + "_copy_" + System.currentTimeMillis());
        target.setStatus(0);
        target.setVersion(1);
        target.setCreateTime(null);
        target.setUpdateTime(null);
        save(target);
        return target.getId();
    }

    @Override
    public void saveTemplateDesign(Long id, String designJson) {
        ReportTemplate template = getById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        try {
            Map<String, Object> config = objectMapper.readValue(designJson,
                    new TypeReference<Map<String, Object>>() {});

            // 更新模板名称
            if (config.containsKey("templateName")) {
                template.setTemplateName((String) config.get("templateName"));
            }

            // 更新模板类型
            if (config.containsKey("templateType")) {
                Object templateType = config.get("templateType");
                if (templateType instanceof Integer) {
                    template.setTemplateType((Integer) templateType);
                } else if (templateType instanceof Number) {
                    template.setTemplateType(((Number) templateType).intValue());
                }
            }

            // 更新数据源ID
            if (config.containsKey("datasourceId")) {
                Object datasourceId = config.get("datasourceId");
                if (datasourceId instanceof Long) {
                    template.setDatasourceId((Long) datasourceId);
                } else if (datasourceId instanceof Number) {
                    template.setDatasourceId(((Number) datasourceId).longValue());
                }
            }

            // 更新查询SQL
            if (config.containsKey("querySql")) {
                template.setQuerySql((String) config.get("querySql"));
            }

            // 更新参数配置
            if (config.containsKey("paramConfig")) {
                Object paramConfig = config.get("paramConfig");
                if (paramConfig instanceof String) {
                    // 前端传递的是 JSON 字符串
                    List<Map<String, Object>> paramList = objectMapper.readValue(
                            (String) paramConfig, new TypeReference<List<Map<String, Object>>>() {});
                    template.setParamConfig(paramList);
                } else if (paramConfig instanceof List) {
                    template.setParamConfig((List<Map<String, Object>>) paramConfig);
                }
            }

            // 更新样式配置
            if (config.containsKey("styleConfig")) {
                template.setStyleConfig((Map<String, Object>) config.get("styleConfig"));
            }

            // 保存完整的模板配置（包含 spreadsheetData 等设计器数据）
            template.setTemplateConfig(config);

            updateById(template);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.TEMPLATE_PARSE_ERROR, "模板配置解析失败: " + e.getMessage());
        }
    }

    @Override
    public List<ReportTemplateVO> listUserTemplates() {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询用户有查看权限的模板ID (permissionType >= 1)
        List<Long> templateIds = reportPermissionMapper.selectTemplateIdsByUserId(userId, 1);

        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportTemplate::getStatus, 1)
                .orderByAsc(ReportTemplate::getSort)
                .orderByDesc(ReportTemplate::getCreateTime);

        // 如果有权限配置，则按权限过滤；否则返回所有已发布模板（兼容无权限配置情况）
        if (templateIds != null && !templateIds.isEmpty()) {
            wrapper.in(ReportTemplate::getId, templateIds);
        }

        return list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 检查用户是否有指定模板的权限
     * @param templateId 模板ID
     * @param permissionType 权限类型 1-查看 2-生成 3-下载 4-编辑
     */
    public boolean checkPermission(Long templateId, Integer permissionType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return reportPermissionMapper.checkUserPermission(userId, templateId, permissionType) > 0;
    }

    private ReportTemplateVO convertToVO(ReportTemplate template) {
        ReportTemplateVO vo = BeanUtil.copyProperties(template, ReportTemplateVO.class);
        
        // 设置类型名称
        if (template.getTemplateType() != null) {
            String[] typeNames = {"", "明细表", "汇总表", "分组统计表", "图表报表"};
            vo.setTemplateTypeName(typeNames[template.getTemplateType()]);
        }
        
        // 设置状态名称
        if (template.getStatus() != null) {
            String[] statusNames = {"草稿", "已发布", "已下线"};
            vo.setStatusName(statusNames[template.getStatus()]);
        }
        
        // 转换参数配置: 将 field 字段映射为 name 字段(兼容前端)
        if (template.getParamConfig() != null && !template.getParamConfig().isEmpty()) {
            List<Map<String, Object>> params = template.getParamConfig();
            for (Map<String, Object> param : params) {
                if (param.containsKey("field") && !param.containsKey("name")) {
                    param.put("name", param.get("field"));
                }
            }
            vo.setParams(params);
        }
        
        return vo;
    }

    @Override
    public void incrementUseCount(Long id) {
        ReportTemplate template = getById(id);
        if (template != null) {
            Integer currentCount = template.getUseCount();
            template.setUseCount(currentCount == null ? 1 : currentCount + 1);
            updateById(template);
        }
    }
}
