package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表分类实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report_category")
public class ReportCategory extends BaseEntity {

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 0-禁用 1-正常
     */
    private Integer status;

    /**
     * 图标
     */
    private String icon;
}
