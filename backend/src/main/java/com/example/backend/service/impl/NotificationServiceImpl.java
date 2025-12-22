package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.SysNotification;
import com.example.backend.entity.SysUser;
import com.example.backend.mapper.SysNotificationMapper;
import com.example.backend.mapper.SysUserMapper;
import com.example.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统通知服务实现
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private SysNotificationMapper notificationMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public void send(Long userId, String title, String content, Integer type, Long bizId, String bizType) {
        SysNotification notification = new SysNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setBizId(bizId);
        notification.setBizType(bizType);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    public void sendToAll(String title, String content, Integer type) {
        // 获取所有用户
        List<SysUser> users = userMapper.selectList(null);
        for (SysUser user : users) {
            send(user.getId(), title, content, type, null, null);
        }
    }

    @Override
    public Page<SysNotification> getMyNotifications(int page, int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<SysNotification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, userId)
                .orderByDesc(SysNotification::getCreateTime);
        return notificationMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Long getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, userId)
                .eq(SysNotification::getIsRead, 0);
        return notificationMapper.selectCount(wrapper);
    }

    @Override
    public List<SysNotification> getRecentUnread(int limit) {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, userId)
                .eq(SysNotification::getIsRead, 0)
                .orderByDesc(SysNotification::getCreateTime)
                .last("LIMIT " + limit);
        return notificationMapper.selectList(wrapper);
    }

    @Override
    public void markAsRead(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaUpdateWrapper<SysNotification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysNotification::getId, id)
                .eq(SysNotification::getUserId, userId)
                .set(SysNotification::getIsRead, 1);
        notificationMapper.update(null, wrapper);
    }

    @Override
    public void markAllAsRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaUpdateWrapper<SysNotification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysNotification::getUserId, userId)
                .eq(SysNotification::getIsRead, 0)
                .set(SysNotification::getIsRead, 1);
        notificationMapper.update(null, wrapper);
    }

    @Override
    public void delete(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getId, id)
                .eq(SysNotification::getUserId, userId);
        notificationMapper.delete(wrapper);
    }

    @Override
    public void clearRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, userId)
                .eq(SysNotification::getIsRead, 1);
        notificationMapper.delete(wrapper);
    }
}
