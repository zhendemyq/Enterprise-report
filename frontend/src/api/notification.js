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

// ========== 管理员接口 ==========

// 管理员获取所有通知列表
export function adminGetNotificationList(params) {
  return request({
    url: '/notification/admin/list',
    method: 'get',
    params
  })
}

// 管理员获取通知统计
export function adminGetStats() {
  return request({
    url: '/notification/admin/stats',
    method: 'get'
  })
}

// 管理员发送通知给所有用户
export function adminSendToAll(data) {
  return request({
    url: '/notification/admin/send-all',
    method: 'post',
    data
  })
}

// 管理员发送通知给指定用户
export function adminSendNotification(data) {
  return request({
    url: '/notification/admin/send',
    method: 'post',
    data
  })
}

// 管理员删除通知
export function adminDeleteNotification(id) {
  return request({
    url: `/notification/admin/${id}`,
    method: 'delete'
  })
}

// 管理员批量删除通知
export function adminBatchDeleteNotifications(ids) {
  return request({
    url: '/notification/admin/batch',
    method: 'delete',
    data: ids
  })
}
