package com.example.backend.dto;

import lombok.Data;

/**
 * 模板权限DTO - 用于细粒度权限控制
 */
@Data
public class TemplatePermissionDTO {

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 查看权限
     */
    private Boolean view = true;

    /**
     * 生成权限
     */
    private Boolean generate = true;

    /**
     * 下载权限
     */
    private Boolean download = true;

    /**
     * 编辑权限
     */
    private Boolean edit = true;
}
