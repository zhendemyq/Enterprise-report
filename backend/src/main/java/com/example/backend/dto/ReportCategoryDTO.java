package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 报表分类DTO
 */
@Data
public class ReportCategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @NotBlank(message = "分类编码不能为空")
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
     * 状态
     */
    private Integer status;

    /**
     * 图标
     */
    private String icon;
}
