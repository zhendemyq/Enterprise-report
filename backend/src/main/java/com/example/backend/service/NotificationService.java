package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.SysNotification;

import java.util.List;

/**
 * 系统通知服务接口
 */
public interface NotificationService {

    /**
     * 发送通知
     */
    void send(Long userId, String title, String content, Integer type, Long bizId, String bizType);

    /**
     * 发送系统通知给所有用户
     */
    void sendToAll(String title, String content, Integer type);

    /**
     * 获取当前用户的通知列表
     */
    Page<SysNotification> getMyNotifications(int page, int size);

    /**
     * 获取当前用户未读通知数量
     */
    Long getUnreadCount();

    /**
     * 获取当前用户最近的未读通知
     */
    List<SysNotification> getRecentUnread(int limit);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long id);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead();

    /**
     * 删除通知
     */
    void delete(Long id);

    /**
     * 清空所有已读通知
     */
    void clearRead();
}
