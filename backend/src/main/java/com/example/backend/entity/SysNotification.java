package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统通知实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notification")
public class SysNotification extends BaseEntity {

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型 1-系统通知 2-报表通知 3-任务通知
     */
    private Integer type;

    /**
     * 关联业务ID（如报表ID、任务ID等）
     */
    private Long bizId;

    /**
     * 关联业务类型
     */
    private String bizType;

    /**
     * 是否已读 0-未读 1-已读
     */
    private Integer isRead;
}
