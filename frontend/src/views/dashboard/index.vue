<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1 class="welcome-title">{{ greeting }}，{{ userStore.nickname || '用户' }}</h1>
          <p class="welcome-subtitle">欢迎回来，这是您的报表系统工作台</p>
        </div>
        <div class="welcome-date">
          <div class="date-display">
            <span class="date-day">{{ currentDate.day }}</span>
            <div class="date-info">
              <span class="date-weekday">{{ currentDate.weekday }}</span>
              <span class="date-month">{{ currentDate.month }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div 
        v-for="stat in statsData" 
        :key="stat.title"
        class="stat-card"
        :style="{ '--accent-color': stat.color }"
      >
        <div class="stat-icon">
          <el-icon :size="24"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-title">{{ stat.title }}</span>
        </div>
        <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
          <el-icon><component :is="stat.trend > 0 ? 'Top' : 'Bottom'" /></el-icon>
          <span>{{ Math.abs(stat.trend) }}%</span>
        </div>
      </div>
    </div>
    
    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- 左侧 -->
      <div class="content-left">
        <!-- 快捷操作 -->
        <div class="section-card quick-actions">
          <div class="card-header">
            <h3 class="card-title">快捷操作</h3>
          </div>
          <div class="actions-grid">
            <div 
              v-for="action in quickActions" 
              :key="action.title"
              class="action-item"
              @click="handleAction(action)"
            >
              <div class="action-icon" :style="{ background: action.bgColor }">
                <el-icon :size="24" :color="action.iconColor">
                  <component :is="action.icon" />
                </el-icon>
              </div>
              <span class="action-title">{{ action.title }}</span>
            </div>
          </div>
        </div>
        
        <!-- 报表生成趋势 -->
        <div class="section-card chart-section">
          <div class="card-header">
            <h3 class="card-title">报表生成趋势</h3>
            <div class="card-actions">
              <el-radio-group v-model="chartPeriod" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">本年</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="chart-container">
            <v-chart class="chart" :option="chartOption" autoresize />
          </div>
        </div>
      </div>
      
      <!-- 右侧 -->
      <div class="content-right">
        <!-- 最近报表 -->
        <div class="section-card recent-reports">
          <div class="card-header">
            <h3 class="card-title">最近生成</h3>
            <el-button type="primary" size="small" round @click="goToRecords">
              查看全部
              <el-icon class="ml-sm"><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="reports-list">
            <div 
              v-for="report in recentReports" 
              :key="report.id"
              class="report-item"
            >
              <div class="report-icon" :class="report.fileType">
                <el-icon><Document /></el-icon>
              </div>
              <div class="report-info">
                <span class="report-name">{{ report.reportName }}</span>
                <span class="report-meta">
                  {{ report.templateName }} · {{ formatTime(report.createTime) }}
                </span>
              </div>
              <div class="report-status" :class="getStatusClass(report.status)">
                {{ getStatusText(report.status) }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- 热门模板 -->
        <div class="section-card popular-templates">
          <div class="card-header">
            <h3 class="card-title">热门模板</h3>
          </div>
          <div class="templates-list">
            <div 
              v-for="(template, index) in popularTemplates" 
              :key="template.id"
              class="template-item"
            >
              <div class="template-rank" :class="{ 'top-three': index < 3 }">
                {{ index + 1 }}
              </div>
              <div class="template-info">
                <span class="template-name">{{ template.templateName }}</span>
                <span class="template-category">{{ template.categoryName }}</span>
              </div>
              <div class="template-count">
                <span>{{ template.usageCount || 0 }}</span>
                <span class="count-label">次使用</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import dayjs from 'dayjs'
import { getStats, getRecentReports, getPopularTemplates, getReportTrend } from '@/api/dashboard'

// 定义组件名称，用于 keep-alive 缓存
defineOptions({
  name: 'Dashboard'
})

use([CanvasRenderer, LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

const router = useRouter()
const userStore = useUserStore()

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
})

// 当前日期
const currentDate = computed(() => {
  const now = dayjs()
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return {
    day: now.format('DD'),
    weekday: weekdays[now.day()],
    month: now.format('YYYY年MM月')
  }
})

// 统计数据
const statsData = ref([
  { title: '报表模板', value: '0', icon: 'Files', color: '#007AFF', trend: 0 },
  { title: '今日生成', value: '0', icon: 'Printer', color: '#34C759', trend: 0 },
  { title: '定时任务', value: '0', icon: 'Timer', color: '#FF9500', trend: 0 },
  { title: '数据源', value: '0', icon: 'Connection', color: '#5856D6', trend: 0 }
])

// 快捷操作
const quickActions = [
  { title: '新建模板', icon: 'Plus', bgColor: 'rgba(0, 122, 255, 0.1)', iconColor: '#007AFF', route: '/report/template/design' },
  { title: '生成报表', icon: 'Printer', bgColor: 'rgba(52, 199, 89, 0.1)', iconColor: '#34C759', route: '/report/generate' },
  { title: '数据源', icon: 'Connection', bgColor: 'rgba(255, 149, 0, 0.1)', iconColor: '#FF9500', route: '/datasource/list' },
  { title: '定时任务', icon: 'Timer', bgColor: 'rgba(88, 86, 214, 0.1)', iconColor: '#5856D6', route: '/schedule/list' }
]

// 图表周期
const chartPeriod = ref('week')
const trendData = ref({
  labels: [],
  generateData: [],
  downloadData: []
})

// 监听图表周期变化
watch(chartPeriod, () => {
  loadTrendData()
})

// 图表配置
const chartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.96)',
    borderColor: 'rgba(0, 0, 0, 0.06)',
    borderWidth: 1,
    textStyle: { color: '#1D1D1F' },
    padding: [12, 16],
    borderRadius: 12,
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)'
  },
  legend: {
    bottom: 0,
    textStyle: { color: '#86868B' },
    icon: 'roundRect',
    itemWidth: 12,
    itemHeight: 12,
    itemGap: 24
  },
  grid: {
    left: 0,
    right: 0,
    top: 20,
    bottom: 40,
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: trendData.value.labels,
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#86868B' }
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#86868B' },
    splitLine: { lineStyle: { color: 'rgba(0, 0, 0, 0.04)' } }
  },
  series: [
    {
      name: '报表生成',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: trendData.value.generateData,
      lineStyle: { width: 3, color: '#007AFF' },
      itemStyle: { color: '#007AFF', borderWidth: 2, borderColor: '#fff' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(0, 122, 255, 0.2)' },
            { offset: 1, color: 'rgba(0, 122, 255, 0)' }
          ]
        }
      }
    },
    {
      name: '下载次数',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: trendData.value.downloadData,
      lineStyle: { width: 3, color: '#34C759' },
      itemStyle: { color: '#34C759', borderWidth: 2, borderColor: '#fff' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(52, 199, 89, 0.2)' },
            { offset: 1, color: 'rgba(52, 199, 89, 0)' }
          ]
        }
      }
    }
  ]
}))

// 最近报表
const recentReports = ref([])

// 热门模板
const popularTemplates = ref([])

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getStats()
    if (res.data) {
      statsData.value = [
        { title: '报表模板', value: String(res.data.templateCount || 0), icon: 'Files', color: '#007AFF', trend: 0 },
        { title: '今日生成', value: String(res.data.todayReportCount || 0), icon: 'Printer', color: '#34C759', trend: 0 },
        { title: '定时任务', value: String(res.data.scheduleCount || 0), icon: 'Timer', color: '#FF9500', trend: 0 },
        { title: '数据源', value: String(res.data.datasourceCount || 0), icon: 'Connection', color: '#5856D6', trend: 0 }
      ]
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载最近报表
const loadRecentReports = async () => {
  try {
    const res = await getRecentReports(5)
    recentReports.value = res.data || []
  } catch (error) {
    console.error('加载最近报表失败:', error)
    recentReports.value = []
  }
}

// 加载热门模板
const loadPopularTemplates = async () => {
  try {
    const res = await getPopularTemplates(5)
    popularTemplates.value = res.data || []
  } catch (error) {
    console.error('加载热门模板失败:', error)
    popularTemplates.value = []
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const res = await getReportTrend(chartPeriod.value)
    if (res.data) {
      trendData.value = {
        labels: res.data.dates || [],
        generateData: res.data.counts || [],
        downloadData: [] // 后端暂未返回下载数据，使用空数组
      }
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
    // 设置默认空数据
    trendData.value = {
      labels: [],
      generateData: [],
      downloadData: []
    }
  }
}

// 刷新所有数据
const refreshAllData = () => {
  loadStats()
  loadRecentReports()
  loadPopularTemplates()
  loadTrendData()
}

// 初始化
onMounted(() => {
  refreshAllData()
})

// 页面激活时刷新数据（从其他页面返回时）
onActivated(() => {
  refreshAllData()
})

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('MM-DD HH:mm')
}

// 获取状态类名
const getStatusClass = (status) => {
  const map = { 0: 'pending', 1: 'success', 2: 'error' }
  return map[status]
}

// 获取状态文本
const getStatusText = (status) => {
  const map = { 0: '生成中', 1: '已完成', 2: '失败' }
  return map[status]
}

// 处理快捷操作
const handleAction = (action) => {
  router.push(action.route)
}

// 跳转到记录页
const goToRecords = () => {
  router.push('/report/records')
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
}

// 欢迎区域
.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: $radius-2xl;
  padding: 32px 40px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    width: 300px;
    height: 300px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    top: -100px;
    right: -50px;
  }
  
  &::after {
    content: '';
    position: absolute;
    width: 200px;
    height: 200px;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 50%;
    bottom: -80px;
    right: 200px;
  }
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: 28px;
  font-weight: $font-weight-bold;
  color: #fff;
  margin-bottom: 8px;
}

.welcome-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
}

.date-display {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 16px 24px;
  border-radius: $radius-xl;
}

.date-day {
  font-size: 42px;
  font-weight: $font-weight-bold;
  color: #fff;
  line-height: 1;
}

.date-info {
  display: flex;
  flex-direction: column;
}

.date-weekday {
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: #fff;
}

.date-month {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

// 统计卡片
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: $shadow-sm;
  transition: all $transition-normal;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-md;
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: $radius-lg;
  background: rgba(var(--accent-color), 0.1);
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.02), rgba(0, 0, 0, 0.06));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-color);
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  line-height: 1.2;
}

.stat-title {
  font-size: 14px;
  color: $text-secondary;
  margin-top: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 13px;
  font-weight: $font-weight-medium;
  padding: 4px 8px;
  border-radius: $radius-sm;
  
  &.up {
    color: $success-color;
    background: rgba($success-color, 0.1);
  }
  
  &.down {
    color: $danger-color;
    background: rgba($danger-color, 0.1);
  }
  
  .el-icon {
    font-size: 12px;
  }
}

// 主要内容区
.main-content {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
}

.content-left {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-right {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

// 通用卡片样式
.section-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 24px;
  box-shadow: $shadow-sm;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

// 快捷操作
.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all $transition-normal;
  
  &:hover {
    background: $gray-50;
    transform: translateY(-2px);
    
    .action-icon {
      transform: scale(1.1);
    }
  }
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform $transition-normal;
}

.action-title {
  font-size: 14px;
  font-weight: $font-weight-medium;
  color: $text-primary;
}

// 图表区域
.chart-section {
  flex: 1;
  
  :deep(.el-radio-group) {
    .el-radio-button__inner {
      border-radius: $radius-md;
      border: none;
      padding: 6px 16px;
      background: transparent;
      
      &:hover {
        color: $primary-color;
      }
    }
    
    .el-radio-button__original-radio:checked + .el-radio-button__inner {
      background: rgba($primary-color, 0.1);
      color: $primary-color;
      box-shadow: none;
    }
  }
}

.chart-container {
  height: 320px;
}

.chart {
  width: 100%;
  height: 100%;
}

// 最近报表
.reports-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.report-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: $radius-lg;
  transition: background $transition-fast;
  cursor: pointer;
  
  &:hover {
    background: $gray-50;
  }
}

.report-icon {
  width: 40px;
  height: 40px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.xlsx {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }
  
  &.pdf {
    background: rgba($danger-color, 0.1);
    color: $danger-color;
  }
}

.report-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.report-name {
  font-size: 14px;
  font-weight: $font-weight-medium;
  color: $text-primary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.report-meta {
  font-size: 12px;
  color: $text-secondary;
  margin-top: 2px;
}

.report-status {
  font-size: 12px;
  font-weight: $font-weight-medium;
  padding: 4px 10px;
  border-radius: $radius-sm;
  
  &.success {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }
  
  &.pending {
    background: rgba($warning-color, 0.1);
    color: $warning-color;
  }
  
  &.error {
    background: rgba($danger-color, 0.1);
    color: $danger-color;
  }
}

// 热门模板
.templates-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.template-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: $radius-lg;
  transition: background $transition-fast;
  cursor: pointer;
  
  &:hover {
    background: $gray-50;
  }
}

.template-rank {
  width: 28px;
  height: 28px;
  border-radius: $radius-sm;
  background: $gray-100;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: $font-weight-semibold;
  color: $text-secondary;
  
  &.top-three {
    background: linear-gradient(135deg, $primary-color, $info-color);
    color: #fff;
  }
}

.template-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.template-name {
  font-size: 14px;
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.template-category {
  font-size: 12px;
  color: $text-secondary;
  margin-top: 2px;
}

.template-count {
  text-align: right;
  
  span:first-child {
    font-size: 16px;
    font-weight: $font-weight-semibold;
    color: $text-primary;
  }
  
  .count-label {
    font-size: 12px;
    color: $text-secondary;
    margin-left: 4px;
  }
}

// 响应式
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .main-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
