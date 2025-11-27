import request from '@/utils/request'

// 获取所有数据源列表
export function listDatasources() {
  return request({
    url: '/report/datasource/list',
    method: 'get'
  })
}

// 获取数据源详情
export function getDatasourceDetail(id) {
  return request({
    url: `/report/datasource/${id}`,
    method: 'get'
  })
}

// 创建数据源
export function createDatasource(data) {
  return request({
    url: '/report/datasource',
    method: 'post',
    data
  })
}

// 更新数据源
export function updateDatasource(id, data) {
  return request({
    url: `/report/datasource/${id}`,
    method: 'put',
    data
  })
}

// 删除数据源
export function deleteDatasource(id) {
  return request({
    url: `/report/datasource/${id}`,
    method: 'delete'
  })
}

// 测试数据源连接
export function testConnection(id) {
  return request({
    url: `/report/datasource/${id}/test`,
    method: 'post'
  })
}

// 获取数据源的表列表
export function getTables(id) {
  return request({
    url: `/report/datasource/${id}/tables`,
    method: 'get'
  })
}

// 获取表的字段列表
export function getTableColumns(id, tableName) {
  return request({
    url: `/report/datasource/${id}/tables/${tableName}/columns`,
    method: 'get'
  })
}

// 执行SQL查询
export function executeQuery(id, sql, params = {}) {
  return request({
    url: `/report/datasource/${id}/query`,
    method: 'post',
    params: { sql },
    data: params
  })
}
