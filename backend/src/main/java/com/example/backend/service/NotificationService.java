package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.SysNotification;
import com.example.backend.vo.NotificationVO;

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
     * 发送通知给指定用户列表
     */
    void sendToUsers(List<Long> userIds, String title, String content, Integer type);

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

    // ========== 管理员功能 ==========

    /**
     * 管理员获取所有通知列表
     */
    Page<SysNotification> getAllNotifications(int page, int size, Long userId, Integer type, Integer isRead);

    /**
     * 管理员获取所有通知列表（带用户名）
     */
    Page<NotificationVO> getAllNotificationsWithUsername(int page, int size, Long userId, Integer type, Integer isRead);

    /**
     * 管理员删除通知
     */
    void adminDelete(Long id);

    /**
     * 管理员批量删除通知
     */
    void adminBatchDelete(List<Long> ids);

    /**
     * 获取通知统计信息
     */
    NotificationStats getStats();

    /**
     * 通知统计信息
     */
    class NotificationStats {
        private Long totalCount;
        private Long unreadCount;
        private Long todayCount;

        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
        public Long getUnreadCount() { return unreadCount; }
        public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }
        public Long getTodayCount() { return todayCount; }
        public void setTodayCount(Long todayCount) { this.todayCount = todayCount; }
    }
}
