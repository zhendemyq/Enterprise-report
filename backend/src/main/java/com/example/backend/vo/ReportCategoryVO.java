package com.example.backend.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报表分类VO（树形结构）
 */
@Data
public class ReportCategoryVO {

    private Long id;

    private String categoryName;

    private String categoryCode;

    private Long parentId;

    private String description;

    private Integer sort;

    private Integer status;

    private String statusName;

    private String icon;

    private LocalDateTime createTime;

    /**
     * 子分类列表
     */
    private List<ReportCategoryVO> children;

    /**
     * 该分类下的模板数量
     */
    private Integer templateCount;
}
