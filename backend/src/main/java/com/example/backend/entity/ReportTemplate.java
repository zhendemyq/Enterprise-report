package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 报表模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "report_template", autoResultMap = true)
public class ReportTemplate extends BaseEntity {

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板编码（唯一标识）
     */
    private String templateCode;

    /**
     * 模板类型 1-明细表 2-汇总表 3-分组统计表 4-图表报表
     */
    private Integer templateType;

    /**
     * 模板分类ID
     */
    private Long categoryId;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板配置（Univer设计器JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> templateConfig;

    /**
     * 模板文件路径（Excel模板）
     */
    private String templateFile;

    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * 查询SQL
     */
    private String querySql;

    /**
     * 参数配置（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> paramConfig;

    /**
     * 样式配置（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> styleConfig;

    /**
     * 状态 0-草稿 1-已发布 2-已下线
     */
    private Integer status;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 是否需要审核
     */
    private Boolean needAudit;

    /**
     * 排序
     */
    private Integer sort;
}
