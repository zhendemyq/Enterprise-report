package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.SysNotification;
import com.example.backend.entity.User;
import com.example.backend.mapper.SysNotificationMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.NotificationService;
import com.example.backend.vo.NotificationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统通知服务实现
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private SysNotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

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
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            send(user.getId(), title, content, type, null, null);
        }
    }

    @Override
    public void sendToUsers(List<Long> userIds, String title, String content, Integer type) {
        for (Long userId : userIds) {
            send(userId, title, content, type, null, null);
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

    // ========== 管理员功能实现 ==========

    @Override
    public Page<SysNotification> getAllNotifications(int page, int size, Long userId, Integer type, Integer isRead) {
        Page<SysNotification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(SysNotification::getUserId, userId);
        }
        if (type != null) {
            wrapper.eq(SysNotification::getType, type);
        }
        if (isRead != null) {
            wrapper.eq(SysNotification::getIsRead, isRead);
        }
        wrapper.orderByDesc(SysNotification::getCreateTime);
        return notificationMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Page<NotificationVO> getAllNotificationsWithUsername(int page, int size, Long userId, Integer type, Integer isRead) {
        Page<SysNotification> notificationPage = getAllNotifications(page, size, userId, type, isRead);

        // 获取所有用户ID
        List<Long> userIds = notificationPage.getRecords().stream()
                .map(SysNotification::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        // 转换为VO
        Page<NotificationVO> voPage = new Page<>(page, size, notificationPage.getTotal());
        voPage.setRecords(notificationPage.getRecords().stream().map(n -> {
            NotificationVO vo = new NotificationVO();
            BeanUtils.copyProperties(n, vo);
            User user = userMap.get(n.getUserId());
            vo.setUsername(user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "用户" + n.getUserId());
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public void adminDelete(Long id) {
        notificationMapper.deleteById(id);
    }

    @Override
    public void adminBatchDelete(List<Long> ids) {
        notificationMapper.deleteBatchIds(ids);
    }

    @Override
    public NotificationStats getStats() {
        NotificationStats stats = new NotificationStats();

        // 总数
        stats.setTotalCount(notificationMapper.selectCount(null));

        // 未读数
        LambdaQueryWrapper<SysNotification> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(SysNotification::getIsRead, 0);
        stats.setUnreadCount(notificationMapper.selectCount(unreadWrapper));

        // 今日发送数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<SysNotification> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(SysNotification::getCreateTime, todayStart);
        stats.setTodayCount(notificationMapper.selectCount(todayWrapper));

        return stats;
    }
}
