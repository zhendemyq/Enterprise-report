package com.example.backend.vo;

import lombok.Data;

import java.util.List;

/**
 * 权限树节点VO
 */
@Data
public class PermissionTreeVO {

    /**
     * 节点ID（模板ID）
     */
    private Long id;

    /**
     * 节点名称
     */
    private String label;

    /**
     * 父节点ID
     */
    private Long parentId;

    /**
     * 节点类型 category-分类 template-模板
     */
    private String type;

    /**
     * 子节点
     */
    private List<PermissionTreeVO> children;
}
