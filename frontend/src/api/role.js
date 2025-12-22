import request from '@/utils/request'

// 获取所有角色列表
export function listRoles() {
  return request({
    url: '/role/list',
    method: 'get'
  })
}

// 分页查询角色
export function pageRoles(params) {
  return request({
    url: '/role/page',
    method: 'get',
    params
  })
}

// 获取角色详情
export function getRoleDetail(id) {
  return request({
    url: `/role/${id}`,
    method: 'get'
  })
}

// 创建角色
export function createRole(data) {
  return request({
    url: '/role',
    method: 'post',
    data
  })
}

// 更新角色
export function updateRole(id, data) {
  return request({
    url: `/role/${id}`,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/role/${id}`,
    method: 'delete'
  })
}

// 启用/禁用角色
export function toggleRoleStatus(id, status) {
  return request({
    url: `/role/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 获取角色权限
export function getRolePermissions(roleId) {
  return request({
    url: `/role/${roleId}/permissions`,
    method: 'get'
  })
}

// 保存角色权限
export function saveRolePermissions(roleId, permissionIds) {
  return request({
    url: `/role/${roleId}/permissions`,
    method: 'put',
    data: permissionIds
  })
}

// 获取权限树
export function getPermissionTree() {
  return request({
    url: '/permission/tree',
    method: 'get'
  })
}

// 获取角色下的用户列表
export function getRoleUsers(roleId) {
  return request({
    url: `/role/${roleId}/users`,
    method: 'get'
  })
}
