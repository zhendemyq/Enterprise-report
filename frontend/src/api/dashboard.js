import request from '@/utils/request'

// 获取统计数据
export function getStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

// 获取最近生成的报表
export function getRecentReports(limit = 5) {
  return request({
    url: '/dashboard/recent-reports',
    method: 'get',
    params: { limit }
  })
}

// 获取热门模板
export function getPopularTemplates(limit = 5) {
  return request({
    url: '/dashboard/popular-templates',
    method: 'get',
    params: { limit }
  })
}

// 获取报表生成趋势
export function getReportTrend(period = 'week') {
  return request({
    url: '/dashboard/trend',
    method: 'get',
    params: { period }
  })
}
