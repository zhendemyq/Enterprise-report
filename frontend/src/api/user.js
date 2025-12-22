import request from '@/utils/request'

// 分页查询用户
export function pageUsers(params) {
  return request({
    url: '/user/page',
    method: 'get',
    params
  })
}

// 创建用户
export function createUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(id, data) {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

// 修改密码
export function changePassword(userId, oldPassword, newPassword) {
  return request({
    url: '/user/password',
    method: 'put',
    params: { userId, oldPassword, newPassword }
  })
}

// 重置密码
export function resetPassword(userId) {
  return request({
    url: `/user/password/reset/${userId}`,
    method: 'put'
  })
}

// 切换用户状态
export function toggleUserStatus(id, status) {
  return request({
    url: `/user/${id}/status`,
    method: 'put',
    params: { status }
  })
}
