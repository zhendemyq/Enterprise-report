import request from '@/utils/request'

// 获取通知列表
export function getNotificationList(page = 1, size = 10) {
  return request({
    url: '/notification/list',
    method: 'get',
    params: { page, size }
  })
}

// 获取未读通知数量
export function getUnreadCount() {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}

// 获取最近未读通知
export function getRecentNotifications(limit = 5) {
  return request({
    url: '/notification/recent',
    method: 'get',
    params: { limit }
  })
}

// 标记通知为已读
export function markAsRead(id) {
  return request({
    url: `/notification/read/${id}`,
    method: 'put'
  })
}

// 标记所有通知为已读
export function markAllAsRead() {
  return request({
    url: '/notification/read-all',
    method: 'put'
  })
}

// 删除通知
export function deleteNotification(id) {
  return request({
    url: `/notification/${id}`,
    method: 'delete'
  })
}

// 清空已读通知
export function clearReadNotifications() {
  return request({
    url: '/notification/clear-read',
    method: 'delete'
  })
}
