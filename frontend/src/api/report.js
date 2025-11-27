import request from '@/utils/request'

// 生成报表
export function generateReport(data) {
  return request({
    url: '/report/generate',
    method: 'post',
    data
  })
}

// 预览报表
export function previewReport(recordId) {
  return request({
    url: `/report/generate/${recordId}/preview`,
    method: 'get',
    responseType: 'blob'
  })
}

// 下载报表
export function downloadReport(recordId) {
  return request({
    url: `/report/generate/${recordId}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

// 分页查询生成记录
export function pageRecords(params) {
  return request({
    url: '/report/generate/records',
    method: 'get',
    params
  })
}

// 获取记录详情
export function getRecordDetail(id) {
  return request({
    url: `/report/generate/records/${id}`,
    method: 'get'
  })
}

// 删除生成记录
export function deleteRecord(id) {
  return request({
    url: `/report/generate/records/${id}`,
    method: 'delete'
  })
}

// 重新生成报表
export function regenerateReport(recordId) {
  return request({
    url: `/report/generate/records/${recordId}/regenerate`,
    method: 'post'
  })
}
