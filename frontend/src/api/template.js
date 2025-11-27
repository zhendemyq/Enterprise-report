import request from '@/utils/request'

// 分页查询报表模板
export function pageTemplates(params) {
  return request({
    url: '/report/template/page',
    method: 'get',
    params
  })
}

// 获取模板详情
export function getTemplateDetail(id) {
  return request({
    url: `/report/template/${id}`,
    method: 'get'
  })
}

// 创建模板
export function createTemplate(data) {
  return request({
    url: '/report/template',
    method: 'post',
    data
  })
}

// 更新模板
export function updateTemplate(id, data) {
  return request({
    url: `/report/template/${id}`,
    method: 'put',
    data
  })
}

// 删除模板
export function deleteTemplate(id) {
  return request({
    url: `/report/template/${id}`,
    method: 'delete'
  })
}

// 发布模板
export function publishTemplate(id) {
  return request({
    url: `/report/template/${id}/publish`,
    method: 'put'
  })
}

// 下线模板
export function offlineTemplate(id) {
  return request({
    url: `/report/template/${id}/offline`,
    method: 'put'
  })
}

// 复制模板
export function copyTemplate(id, newName) {
  return request({
    url: `/report/template/${id}/copy`,
    method: 'post',
    params: { newName }
  })
}

// 保存模板设计
export function saveTemplateDesign(id, designJson) {
  return request({
    url: `/report/template/${id}/design`,
    method: 'put',
    data: designJson
  })
}

// 获取用户有权限的模板列表
export function listUserTemplates() {
  return request({
    url: '/report/template/user',
    method: 'get'
  })
}

// 根据分类获取模板
export function listByCategory(categoryId) {
  return request({
    url: `/report/template/category/${categoryId}`,
    method: 'get'
  })
}
