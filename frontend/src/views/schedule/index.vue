<template>
  <div class="schedule-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">定时任务</h1>
        <p class="page-desc">配置定时报表生成任务，支持邮件自动发送</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建任务
        </el-button>
      </div>
    </div>
    
    <!-- 筛选区 -->
    <div class="filter-card">
      <div class="filter-form">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索任务名称"
          prefix-icon="Search"
          clearable
          class="search-input"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        
        <el-select
          v-model="queryParams.scheduleType"
          placeholder="调度类型"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="每日" :value="1" />
          <el-option label="每周" :value="2" />
          <el-option label="每月" :value="3" />
          <el-option label="自定义" :value="4" />
        </el-select>
        
        <el-button @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>

        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </div>
    
    <!-- 任务列表 -->
    <div class="schedule-card">
      <el-table
        v-loading="loading"
        :data="scheduleList"
        row-key="id"
        style="width: 100%"
      >
        <el-table-column label="任务信息" min-width="200">
          <template #default="{ row }">
            <div class="task-info">
              <div class="task-icon" :class="{ active: row.status === 1 }">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="task-text">
                <span class="task-name">{{ row.taskName }}</span>
                <span class="task-template">{{ row.templateName }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="调度类型" width="90">
          <template #default="{ row }">
            <el-tag :type="getScheduleTypeTag(row.scheduleType)" size="small">
              {{ getScheduleTypeText(row.scheduleType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Cron表达式" width="165">
          <template #default="{ row }">
            <code class="cron-code">{{ row.cronExpression }}</code>
          </template>
        </el-table-column>

        <el-table-column label="邮件" width="60" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.sendEmail" class="email-icon active"><Message /></el-icon>
            <el-icon v-else class="email-icon"><Message /></el-icon>
          </template>
        </el-table-column>

        <el-table-column label="上次执行" width="140">
          <template #default="{ row }">
            <span v-if="row.lastExecuteTime">{{ formatDate(row.lastExecuteTime) }}</span>
            <span v-else class="text-secondary">-</span>
          </template>
        </el-table-column>

        <el-table-column label="下次执行" width="140">
          <template #default="{ row }">
            <span v-if="row.nextExecuteTime">{{ formatDate(row.nextExecuteTime) }}</span>
            <span v-else class="text-secondary">-</span>
          </template>
        </el-table-column>

        <el-table-column label="统计" width="80">
          <template #default="{ row }">
            <div class="exec-stats">
              <span class="success">{{ row.executeCount - row.failCount }}</span>
              <span class="divider">/</span>
              <span class="fail">{{ row.failCount }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button text type="primary" @click="handleExecute(row)">
                执行
              </el-button>
              <el-button text type="primary" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button text type="primary" @click="handleViewLog(row)">
                日志
              </el-button>
              <el-button text type="danger" @click="handleDelete(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="top"
      >
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="formData.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        
        <el-form-item label="选择模板" prop="templateId">
          <el-select 
            v-model="formData.templateId" 
            placeholder="选择报表模板" 
            filterable 
            style="width: 100%"
          >
            <el-option
              v-for="t in templateList"
              :key="t.id"
              :label="t.templateName"
              :value="t.id"
            />
          </el-select>
        </el-form-item>
        
        <div class="form-row">
          <el-form-item label="调度类型" prop="scheduleType" class="flex-1">
            <el-select v-model="formData.scheduleType" @change="handleScheduleTypeChange">
              <el-option label="每日" :value="1" />
              <el-option label="每周" :value="2" />
              <el-option label="每月" :value="3" />
              <el-option label="自定义" :value="4" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="文件格式" prop="fileType" class="flex-1">
            <el-select v-model="formData.fileType">
              <el-option label="Excel" value="xlsx" />
              <el-option label="PDF" value="pdf" />
              <el-option label="CSV" value="csv" />
            </el-select>
          </el-form-item>
        </div>
        
        <el-form-item label="Cron表达式" prop="cronExpression">
          <el-input v-model="formData.cronExpression" placeholder="如: 0 0 8 * * ?" />
          <div class="cron-presets">
            <span>快捷设置：</span>
            <el-button size="small" text @click="setCron('0 0 8 * * ?')">每天8点</el-button>
            <el-button size="small" text @click="setCron('0 0 9 ? * MON')">每周一9点</el-button>
            <el-button size="small" text @click="setCron('0 0 10 1 * ?')">每月1号10点</el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="邮件通知">
          <el-switch v-model="formData.sendEmail" />
        </el-form-item>
        
        <template v-if="formData.sendEmail">
          <el-form-item label="收件人" prop="emailReceivers">
            <el-input 
              v-model="formData.emailReceivers" 
              placeholder="多个邮箱用逗号分隔"
            />
          </el-form-item>
          
          <el-form-item label="邮件主题" prop="emailSubject">
            <el-input v-model="formData.emailSubject" placeholder="邮件主题" />
          </el-form-item>
          
          <el-form-item label="邮件内容" prop="emailContent">
            <el-input
              v-model="formData.emailContent"
              type="textarea"
              :rows="3"
              placeholder="邮件正文内容"
            />
          </el-form-item>
        </template>
        
        <el-form-item label="任务描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
            placeholder="任务描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 执行日志弹窗 -->
    <el-dialog
      v-model="logDialogVisible"
      title="执行日志"
      width="800px"
    >
      <el-table :data="logList" max-height="400">
        <el-table-column label="执行时间" width="170">
          <template #default="{ row }">
            {{ formatDate(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getLogStatusType(row.status)" size="small">
              {{ getLogStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100">
          <template #default="{ row }">
            {{ row.duration }}ms
          </template>
        </el-table-column>
        <el-table-column label="执行结果">
          <template #default="{ row }">
            <span v-if="row.status === 1" class="text-success">执行成功</span>
            <span v-else-if="row.status === 2" class="text-danger">{{ row.errorMsg || '执行失败' }}</span>
            <span v-else class="text-secondary">执行中...</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { 
  pageSchedules, 
  createSchedule, 
  updateSchedule, 
  deleteSchedule, 
  toggleScheduleStatus, 
  executeSchedule,
  getScheduleLogs 
} from '@/api/schedule'
import { listUserTemplates } from '@/api/template'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  status: null,
  scheduleType: null
})

// 数据
const loading = ref(false)
const scheduleList = ref([])
const templateList = ref([])
const total = ref(0)

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = computed(() => formData.id ? '编辑任务' : '新建任务')
const submitLoading = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  taskName: '',
  templateId: null,
  scheduleType: 1,
  cronExpression: '0 0 8 * * ?',
  fileType: 'xlsx',
  sendEmail: false,
  emailReceivers: '',
  emailSubject: '',
  emailContent: '',
  description: ''
})

const formRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  templateId: [
    { required: true, message: '请选择报表模板', trigger: 'change' }
  ],
  cronExpression: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' }
  ]
}

// 日志弹窗
const logDialogVisible = ref(false)
const logList = ref([])

// 初始化
onMounted(() => {
  loadSchedules()
  loadTemplates()
})

// 加载任务列表
const loadSchedules = async () => {
  loading.value = true
  try {
    // 构建查询参数，过滤掉空值确保搜索功能正常
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    // 只添加非空的搜索参数
    if (queryParams.keyword && queryParams.keyword.trim()) {
      params.keyword = queryParams.keyword.trim()
    }
    if (queryParams.status != null) {
      params.status = queryParams.status
    }
    if (queryParams.scheduleType != null) {
      params.scheduleType = queryParams.scheduleType
    }

    const res = await pageSchedules(params)
    scheduleList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('加载任务列表失败:', error)
    scheduleList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 加载模板列表
const loadTemplates = async () => {
  try {
    const res = await listUserTemplates()
    templateList.value = res.data || []
  } catch (error) {
    console.error('加载模板列表失败:', error)
    templateList.value = []
  }
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  loadSchedules()
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = null
  queryParams.scheduleType = null
  handleSearch()
}

// 新建
const handleCreate = () => {
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 手动执行
const handleExecute = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要立即执行任务「${row.taskName}」吗？`,
      '提示',
      { type: 'warning' }
    )
    await executeSchedule(row.id)
    ElMessage.success('任务已提交执行')
    loadSchedules()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('执行任务失败:', error)
    }
  }
}

// 查看日志
const handleViewLog = async (row) => {
  try {
    const res = await getScheduleLogs(row.id, { pageNum: 1, pageSize: 20 })
    logList.value = res.data?.records || []
    logDialogVisible.value = true
  } catch (error) {
    console.error('加载日志失败:', error)
    logList.value = []
    logDialogVisible.value = true
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除任务「${row.taskName}」吗？`,
      '警告',
      { type: 'error', confirmButtonText: '删除' }
    )
    await deleteSchedule(row.id)
    ElMessage.success('删除成功')
    loadSchedules()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    await toggleScheduleStatus(row.id, row.status)
    ElMessage.success(`任务「${row.taskName}」已${row.status ? '启用' : '禁用'}`)
  } catch (error) {
    console.error('状态变更失败:', error)
    // 回滚状态
    row.status = row.status ? 0 : 1
  }
}

// 调度类型变更
const handleScheduleTypeChange = (type) => {
  const cronMap = {
    1: '0 0 8 * * ?',
    2: '0 0 9 ? * MON',
    3: '0 0 10 1 * ?',
    4: ''
  }
  formData.cronExpression = cronMap[type] || ''
}

// 设置Cron
const setCron = (cron) => {
  formData.cronExpression = cron
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          await updateSchedule(formData.id, formData)
          ElMessage.success('更新成功')
        } else {
          await createSchedule(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadSchedules()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    taskName: '',
    templateId: null,
    scheduleType: 1,
    cronExpression: '0 0 8 * * ?',
    fileType: 'xlsx',
    sendEmail: false,
    emailReceivers: '',
    emailSubject: '',
    emailContent: '',
    description: ''
  })
}

// 分页
const handleSizeChange = () => {
  queryParams.pageNum = 1
  loadSchedules()
}

const handleCurrentChange = () => {
  loadSchedules()
}

// 工具函数
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getScheduleTypeText = (type) => {
  const map = { 1: '每日', 2: '每周', 3: '每月', 4: '自定义' }
  return map[type] || '未知'
}

const getScheduleTypeTag = (type) => {
  const map = { 1: 'success', 2: 'primary', 3: 'warning', 4: 'info' }
  return map[type] || 'info'
}

// 日志状态映射 (0=执行中, 1=成功, 2=失败)
const getLogStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getLogStatusText = (status) => {
  const map = { 0: '执行中', 1: '成功', 2: '失败' }
  return map[status] || '未知'
}
</script>

<style lang="scss" scoped>
.schedule-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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
  width: 140px;
}

.schedule-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 20px;
  box-shadow: $shadow-sm;
  margin-bottom: 24px;
}

.task-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.task-icon {
  width: 40px;
  height: 40px;
  border-radius: $radius-md;
  background: $gray-100;
  color: $text-secondary;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.active {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }
}

.task-text {
  display: flex;
  flex-direction: column;
}

.task-name {
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.task-template {
  font-size: 12px;
  color: $text-secondary;
}

.cron-code {
  font-family: $font-family-mono;
  font-size: 12px;
  background: $gray-100;
  padding: 4px 8px;
  border-radius: $radius-sm;
}

.email-icon {
  font-size: 18px;
  color: $text-tertiary;
  
  &.active {
    color: $primary-color;
  }
}

.exec-stats {
  font-size: 13px;
  
  .success {
    color: $success-color;
    font-weight: $font-weight-medium;
  }
  
  .fail {
    color: $danger-color;
    font-weight: $font-weight-medium;
  }
  
  .divider {
    color: $text-tertiary;
    margin: 0 4px;
  }
}

.table-actions {
  display: flex;
  gap: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  background: $bg-primary;
  border-radius: $radius-xl;
  box-shadow: $shadow-sm;
}

.form-row {
  display: flex;
  gap: 16px;
  
  .el-form-item {
    margin-bottom: 18px;
  }
  
  :deep(.el-select) {
    width: 100%;
  }
}

.cron-presets {
  margin-top: 8px;
  font-size: 12px;
  color: $text-secondary;
  
  span {
    margin-right: 8px;
  }
}
</style>
