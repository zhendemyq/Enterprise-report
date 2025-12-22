package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.Result;
import com.example.backend.entity.SysNotification;
import com.example.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 */
@Tag(name = "通知管理", description = "系统通知相关接口")
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "获取通知列表")
    @GetMapping("/list")
    public Result<Page<SysNotification>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(notificationService.getMyNotifications(page, size));
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(notificationService.getUnreadCount());
    }

    @Operation(summary = "获取最近未读通知")
    @GetMapping("/recent")
    public Result<Map<String, Object>> getRecent(@RequestParam(defaultValue = "5") Integer limit) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", notificationService.getRecentUnread(limit));
        result.put("unreadCount", notificationService.getUnreadCount());
        return Result.success(result);
    }

    @Operation(summary = "标记通知为已读")
    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success();
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return Result.success();
    }

    @Operation(summary = "清空已读通知")
    @DeleteMapping("/clear-read")
    public Result<Void> clearRead() {
        notificationService.clearRead();
        return Result.success();
    }

    // ========== 管理员接口 ==========

    @Operation(summary = "管理员获取所有通知列表")
    @SaCheckRole("ADMIN")
    @GetMapping("/admin/list")
    public Result<Page<SysNotification>> adminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer isRead) {
        return Result.success(notificationService.getAllNotifications(page, size, userId, type, isRead));
    }

    @Operation(summary = "管理员获取通知统计")
    @SaCheckRole("ADMIN")
    @GetMapping("/admin/stats")
    public Result<NotificationService.NotificationStats> adminStats() {
        return Result.success(notificationService.getStats());
    }

    @Operation(summary = "管理员发送通知给所有用户")
    @SaCheckRole("ADMIN")
    @PostMapping("/admin/send-all")
    public Result<Void> adminSendToAll(@RequestBody SendNotificationRequest request) {
        notificationService.sendToAll(request.getTitle(), request.getContent(), request.getType());
        return Result.success();
    }

    @Operation(summary = "管理员发送通知给指定用户")
    @SaCheckRole("ADMIN")
    @PostMapping("/admin/send")
    public Result<Void> adminSend(@RequestBody SendNotificationRequest request) {
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            notificationService.sendToUsers(request.getUserIds(), request.getTitle(), request.getContent(), request.getType());
        }
        return Result.success();
    }

    @Operation(summary = "管理员删除通知")
    @SaCheckRole("ADMIN")
    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDelete(@PathVariable Long id) {
        notificationService.adminDelete(id);
        return Result.success();
    }

    @Operation(summary = "管理员批量删除通知")
    @SaCheckRole("ADMIN")
    @DeleteMapping("/admin/batch")
    public Result<Void> adminBatchDelete(@RequestBody List<Long> ids) {
        notificationService.adminBatchDelete(ids);
        return Result.success();
    }

    /**
     * 发送通知请求
     */
    public static class SendNotificationRequest {
        private String title;
        private String content;
        private Integer type;
        private List<Long> userIds;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Integer getType() { return type; }
        public void setType(Integer type) { this.type = type; }
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
    }
}
