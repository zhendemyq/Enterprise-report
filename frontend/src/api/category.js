import request from '@/utils/request'

// 获取分类树
export function getCategoryTree() {
  return request({
    url: '/report/category/tree',
    method: 'get'
  })
}

// 获取分类列表
export function listCategories() {
  return request({
    url: '/report/category/list',
    method: 'get'
  })
}

// 获取分类详情
export function getCategoryDetail(id) {
  return request({
    url: `/report/category/${id}`,
    method: 'get'
  })
}

// 创建分类
export function createCategory(data) {
  return request({
    url: '/report/category',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(id, data) {
  return request({
    url: `/report/category/${id}`,
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/report/category/${id}`,
    method: 'delete'
  })
}

// 移动分类排序
export function moveCategory(id, targetParentId, sort) {
  return request({
    url: `/report/category/${id}/move`,
    method: 'put',
    params: { targetParentId, sort }
  })
}
