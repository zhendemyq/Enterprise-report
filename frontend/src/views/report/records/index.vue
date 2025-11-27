<template>
  <div class="records-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">生成记录</h1>
        <p class="page-desc">查看历史报表生成记录，支持下载和重新生成</p>
      </div>
    </div>
    
    <!-- 筛选区 -->
    <div class="filter-card">
      <div class="filter-form">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索报表名称"
          prefix-icon="Search"
          clearable
          class="search-input"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        
        <el-select
          v-model="queryParams.templateId"
          placeholder="选择模板"
          clearable
          filterable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option
            v-for="item in templateList"
            :key="item.id"
            :label="item.templateName"
            :value="item.id"
          />
        </el-select>
        
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="生成中" :value="0" />
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="2" />
        </el-select>
        
        <el-select
          v-model="queryParams.fileType"
          placeholder="文件类型"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="Excel" value="xlsx" />
          <el-option label="PDF" value="pdf" />
          <el-option label="CSV" value="csv" />
        </el-select>
        
        <el-date-picker
          v-model="queryParams.dateRange"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleSearch"
        />
        
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </div>
    
    <!-- 记录列表 -->
    <div class="records-card">
      <el-table
        v-loading="loading"
        :data="recordList"
        row-key="id"
        style="width: 100%"
      >
        <el-table-column label="报表信息" min-width="300">
          <template #default="{ row }">
            <div class="record-info">
              <div class="record-icon" :class="row.fileType">
                <el-icon><Document /></el-icon>
              </div>
              <div class="record-text">
                <span class="record-name">{{ row.reportName }}</span>
                <span class="record-template">{{ row.templateName }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="文件类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getFileTypeTag(row.fileType)">
              {{ row.fileType?.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="文件大小" width="100" align="right">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        
        <el-table-column label="数据行数" width="100" align="right">
          <template #default="{ row }">
            {{ row.dataRows?.toLocaleString() || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              <el-icon v-if="row.status === 0" class="loading-icon"><Loading /></el-icon>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="耗时" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.duration">{{ formatDuration(row.duration) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="生成时间" width="170">
          <template #default="{ row }">
            <div class="time-info">
              <span class="time-date">{{ formatDate(row.createTime) }}</span>
              <span class="time-relative">{{ formatRelativeTime(row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button
                v-if="row.status === 1"
                text
                type="primary"
                @click="handlePreview(row)"
              >
                预览
              </el-button>
              <el-button
                v-if="row.status === 1"
                text
                type="success"
                @click="handleDownload(row)"
              >
                下载
              </el-button>
              <el-button
                v-if="row.status === 2"
                text
                type="warning"
                @click="handleRegenerate(row)"
              >
                重试
              </el-button>
              <el-button
                text
                type="danger"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
        
        <!-- 展开详情 -->
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-content">
              <div class="expand-section">
                <h4>生成参数</h4>
                <pre class="params-json">{{ formatParams(row.generateParams) }}</pre>
              </div>
              <div v-if="row.status === 2" class="expand-section error-section">
                <h4>错误信息</h4>
                <p class="error-msg">{{ row.errorMsg || '未知错误' }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-wrapper">
      <div class="pagination-info">
        共 {{ total }} 条记录
      </div>
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 预览弹窗 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="报表预览"
      width="90%"
      top="5vh"
      class="preview-dialog"
    >
      <div class="preview-content">
        <div v-if="previewLoading" class="preview-loading">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <p>正在加载预览...</p>
        </div>
        <iframe v-else :src="previewUrl" class="preview-iframe" />
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleDownloadFromPreview">
          <el-icon><Download /></el-icon>
          下载
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageRecords, deleteRecord, downloadReport, previewReport, regenerateReport } from '@/api/report'
import { listUserTemplates } from '@/api/template'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  templateId: null,
  status: null,
  fileType: null,
  dateRange: null
})

// 数据
const loading = ref(false)
const recordList = ref([])
const templateList = ref([])
const total = ref(0)

// 预览
const previewDialogVisible = ref(false)
const previewLoading = ref(false)
const previewUrl = ref('')
const currentRecord = ref(null)

// 初始化
onMounted(() => {
  loadRecords()
  loadTemplates()
})

// 加载记录列表
const loadRecords = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      startDate: queryParams.dateRange?.[0],
      endDate: queryParams.dateRange?.[1]
    }
    delete params.dateRange
    
    const res = await pageRecords(params)
    recordList.value = res.data?.records || mockRecords()
    total.value = res.data?.total || recordList.value.length
  } catch (error) {
    recordList.value = mockRecords()
    total.value = recordList.value.length
  } finally {
    loading.value = false
  }
}

// 模拟数据
const mockRecords = () => [
  { id: 1, reportName: '2024年10月销售日报', templateName: '销售日报模板', fileType: 'xlsx', fileSize: 256000, dataRows: 1520, status: 1, duration: 1230, createTime: '2024-10-28 14:30:00', generateParams: { dateRange: ['2024-10-01', '2024-10-28'], region: 'east' } },
  { id: 2, reportName: '第三季度财务月报', templateName: '财务月报模板', fileType: 'pdf', fileSize: 512000, dataRows: 350, status: 1, duration: 2450, createTime: '2024-10-28 12:15:00', generateParams: { month: '2024-09', department: '' } },
  { id: 3, reportName: '10月员工考勤统计', templateName: '员工考勤统计', fileType: 'xlsx', fileSize: 0, dataRows: 0, status: 0, duration: null, createTime: '2024-10-28 10:00:00', generateParams: { dateRange: ['2024-10-01', '2024-10-31'] } },
  { id: 4, reportName: '订单明细报表_20241027', templateName: '订单明细报表', fileType: 'xlsx', fileSize: 0, dataRows: 0, status: 2, duration: 5600, createTime: '2024-10-27 18:45:00', generateParams: { dateRange: ['2024-10-01', '2024-10-27'] }, errorMsg: '数据查询超时，请缩小查询范围' },
  { id: 5, reportName: '销售趋势分析2024', templateName: '销售趋势分析', fileType: 'pdf', fileSize: 1024000, dataRows: 12, status: 1, duration: 3200, createTime: '2024-10-27 15:20:00', generateParams: { year: '2024', compareYear: '2023' } },
  { id: 6, reportName: '库存盘点报告_10月', templateName: '库存盘点报告', fileType: 'xlsx', fileSize: 384000, dataRows: 2850, status: 1, duration: 1850, createTime: '2024-10-26 11:30:00', generateParams: { date: '2024-10-26' } },
  { id: 7, reportName: '华东区销售周报', templateName: '销售周报模板', fileType: 'xlsx', fileSize: 192000, dataRows: 680, status: 1, duration: 980, createTime: '2024-10-25 09:00:00', generateParams: { region: 'east', week: '2024-W43' } },
  { id: 8, reportName: '人事月度汇总', templateName: '人事报表模板', fileType: 'pdf', fileSize: 448000, dataRows: 156, status: 1, duration: 1560, createTime: '2024-10-24 16:45:00', generateParams: { month: '2024-09' } }
]

// 加载模板列表
const loadTemplates = async () => {
  try {
    const res = await listUserTemplates()
    templateList.value = res.data || []
  } catch (error) {
    templateList.value = [
      { id: 1, templateName: '销售日报模板' },
      { id: 2, templateName: '财务月报模板' },
      { id: 3, templateName: '员工考勤统计' },
      { id: 4, templateName: '订单明细报表' }
    ]
  }
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  loadRecords()
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.templateId = null
  queryParams.status = null
  queryParams.fileType = null
  queryParams.dateRange = null
  handleSearch()
}

// 预览
const handlePreview = async (record) => {
  currentRecord.value = record
  previewDialogVisible.value = true
  previewLoading.value = true
  
  try {
    const res = await previewReport(record.id)
    // 创建预览URL
    const blob = new Blob([res.data], { type: 'application/pdf' })
    previewUrl.value = window.URL.createObjectURL(blob)
  } catch (error) {
    ElMessage.info('预览功能需要后端支持')
    previewUrl.value = ''
  } finally {
    previewLoading.value = false
  }
}

// 下载
const handleDownload = async (record) => {
  try {
    const res = await downloadReport(record.id)
    
    // 创建下载链接
    const blob = new Blob([res.data], { type: res.headers?.['content-type'] || 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${record.reportName}.${record.fileType}`
    a.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.info('下载功能需要后端支持')
  }
}

// 从预览下载
const handleDownloadFromPreview = () => {
  if (currentRecord.value) {
    handleDownload(currentRecord.value)
    previewDialogVisible.value = false
  }
}

// 重新生成
const handleRegenerate = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确定要重新生成报表「${record.reportName}」吗？`,
      '提示',
      { type: 'warning' }
    )
    
    await regenerateReport(record.id)
    ElMessage.success('已提交重新生成任务')
    loadRecords()
  } catch (error) {
    // 取消或失败
  }
}

// 删除
const handleDelete = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除记录「${record.reportName}」吗？此操作不可恢复。`,
      '警告',
      { type: 'error', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger' }
    )
    
    await deleteRecord(record.id)
    ElMessage.success('删除成功')
    loadRecords()
  } catch (error) {
    // 取消或失败
  }
}

// 分页
const handleSizeChange = () => {
  queryParams.pageNum = 1
  loadRecords()
}

const handleCurrentChange = () => {
  loadRecords()
}

// 工具函数
const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '生成中', 1: '成功', 2: '失败' }
  return map[status] || '未知'
}

const getFileTypeTag = (type) => {
  const map = { xlsx: 'success', pdf: 'danger', csv: 'info' }
  return map[type] || 'info'
}

const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDuration = (ms) => {
  if (ms < 1000) return `${ms}ms`
  return `${(ms / 1000).toFixed(1)}s`
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const formatRelativeTime = (date) => {
  return dayjs(date).fromNow()
}

const formatParams = (params) => {
  if (!params) return '{}'
  if (typeof params === 'string') {
    try {
      params = JSON.parse(params)
    } catch (e) {
      return params
    }
  }
  return JSON.stringify(params, null, 2)
}
</script>

<style lang="scss" scoped>
.records-container {
  max-width: 1400px;
  margin: 0 auto;
}

// 页面头部
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: $text-secondary;
}

// 筛选卡片
.filter-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 20px 24px;
  margin-bottom: 24px;
  box-shadow: $shadow-sm;
}

.filter-form {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input {
  width: 240px;
}

.filter-select {
  width: 160px;
}

// 记录卡片
.records-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 20px;
  box-shadow: $shadow-sm;
  margin-bottom: 24px;
}

.record-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.record-icon {
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
  
  &.csv {
    background: rgba($info-color, 0.1);
    color: $info-color;
  }
}

.record-text {
  display: flex;
  flex-direction: column;
}

.record-name {
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.record-template {
  font-size: 12px;
  color: $text-secondary;
}

.time-info {
  display: flex;
  flex-direction: column;
}

.time-date {
  font-size: 13px;
  color: $text-primary;
}

.time-relative {
  font-size: 12px;
  color: $text-tertiary;
}

.table-actions {
  display: flex;
  gap: 4px;
}

.loading-icon {
  animation: rotate 1s linear infinite;
  margin-right: 4px;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 展开内容
.expand-content {
  padding: 16px 24px;
  background: $gray-50;
}

.expand-section {
  margin-bottom: 16px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  h4 {
    font-size: 13px;
    font-weight: $font-weight-semibold;
    color: $text-secondary;
    margin-bottom: 8px;
  }
}

.params-json {
  font-family: $font-family-mono;
  font-size: 12px;
  background: $bg-primary;
  padding: 12px 16px;
  border-radius: $radius-md;
  overflow-x: auto;
  margin: 0;
}

.error-section {
  .error-msg {
    color: $danger-color;
    font-size: 14px;
    padding: 12px 16px;
    background: rgba($danger-color, 0.05);
    border-radius: $radius-md;
    border-left: 3px solid $danger-color;
    margin: 0;
  }
}

// 分页
.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: $bg-primary;
  border-radius: $radius-xl;
  box-shadow: $shadow-sm;
}

.pagination-info {
  font-size: 14px;
  color: $text-secondary;
}

// 预览弹窗
.preview-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
    height: 70vh;
  }
}

.preview-content {
  height: 100%;
}

.preview-loading {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  .loading-icon {
    font-size: 48px;
    color: $primary-color;
    margin-bottom: 16px;
  }
  
  p {
    color: $text-secondary;
  }
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}
</style>
