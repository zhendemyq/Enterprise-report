import request from '@/utils/request'

// 分页查询定时任务
export function pageSchedules(params) {
  return request({
    url: '/report/schedule/page',
    method: 'get',
    params
  })
}

// 获取定时任务详情
export function getScheduleDetail(id) {
  return request({
    url: `/report/schedule/${id}`,
    method: 'get'
  })
}

// 创建定时任务
export function createSchedule(data) {
  return request({
    url: '/report/schedule',
    method: 'post',
    data
  })
}

// 更新定时任务
export function updateSchedule(id, data) {
  return request({
    url: `/report/schedule/${id}`,
    method: 'put',
    data
  })
}

// 删除定时任务
export function deleteSchedule(id) {
  return request({
    url: `/report/schedule/${id}`,
    method: 'delete'
  })
}

// 启用/禁用定时任务
export function toggleScheduleStatus(id, status) {
  return request({
    url: `/report/schedule/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 立即执行任务
export function executeSchedule(id) {
  return request({
    url: `/report/schedule/${id}/execute`,
    method: 'post'
  })
}

// 获取执行日志
export function getScheduleLogs(id, params) {
  return request({
    url: `/report/schedule/${id}/logs`,
    method: 'get',
    params
  })
}
