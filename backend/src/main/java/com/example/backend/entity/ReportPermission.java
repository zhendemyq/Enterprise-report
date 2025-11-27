package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表权限实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report_permission")
public class ReportPermission extends BaseEntity {

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限类型 1-查看 2-生成 3-下载 4-编辑
     */
    private Integer permissionType;
}
