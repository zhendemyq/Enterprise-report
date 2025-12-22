<template>
  <div class="message-manage-container">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon :size="24"><Message /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalCount || 0 }}</span>
          <span class="stat-label">消息总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon unread">
          <el-icon :size="24"><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.unreadCount || 0 }}</span>
          <span class="stat-label">未读消息</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon today">
          <el-icon :size="24"><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.todayCount || 0 }}</span>
          <span class="stat-label">今日发送</span>
        </div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleSendNotification">
          <el-icon><Plus /></el-icon>
          发送通知
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
      <div class="action-right">
        <el-select v-model="queryParams.type" placeholder="通知类型" clearable style="width: 120px" @change="handleSearch">
          <el-option label="系统通知" :value="1" />
          <el-option label="报表通知" :value="2" />
          <el-option label="任务通知" :value="3" />
        </el-select>
        <el-select v-model="queryParams.isRead" placeholder="阅读状态" clearable style="width: 120px" @change="handleSearch">
          <el-option label="未读" :value="0" />
          <el-option label="已读" :value="1" />
        </el-select>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <!-- 消息列表 -->
    <el-table
      v-loading="loading"
      :data="notifications"
      @selection-change="handleSelectionChange"
      class="message-table"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column label="接收用户" prop="userId" width="100">
        <template #default="{ row }">
          <span>用户{{ row.userId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="通知类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getTypeTagType(row.type)" size="small">
            {{ getTypeName(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标题" prop="title" min-width="150" show-overflow-tooltip />
      <el-table-column label="内容" prop="content" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isRead === 1 ? 'info' : 'warning'" size="small">
            {{ row.isRead === 1 ? '已读' : '未读' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发送时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" link size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 发送通知对话框 -->
    <el-dialog
      v-model="sendDialogVisible"
      title="发送通知"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="sendFormRef" :model="sendForm" :rules="sendRules" label-width="100px">
        <el-form-item label="发送对象" prop="sendType">
          <el-radio-group v-model="sendForm.sendType">
            <el-radio label="all">所有用户</el-radio>
            <el-radio label="selected">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="sendForm.sendType === 'selected'" label="选择用户" prop="userIds">
          <el-select
            v-model="sendForm.userIds"
            multiple
            filterable
            placeholder="请选择用户"
            style="width: 100%"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.nickname || user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="sendForm.type" placeholder="请选择通知类型" style="width: 100%">
            <el-option label="系统通知" :value="1" />
            <el-option label="报表通知" :value="2" />
            <el-option label="任务通知" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="sendForm.title" placeholder="请输入通知标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input
            v-model="sendForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入通知内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="sendLoading" @click="handleSendSubmit">
          发送
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Message, Bell, Calendar, Plus, Delete, Refresh } from '@element-plus/icons-vue'
import {
  adminGetNotificationList,
  adminGetStats,
  adminSendToAll,
  adminSendNotification,
  adminDeleteNotification,
  adminBatchDeleteNotifications
} from '@/api/notification'
import { pageUsers } from '@/api/user'
import dayjs from 'dayjs'

// 定义组件名称
defineOptions({
  name: 'MessageManage'
})

// 数据
const loading = ref(false)
const notifications = ref([])
const total = ref(0)
const selectedIds = ref([])
const stats = ref({})
const userList = ref([])

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  userId: null,
  type: null,
  isRead: null
})

// 发送通知
const sendDialogVisible = ref(false)
const sendLoading = ref(false)
const sendFormRef = ref(null)
const sendForm = reactive({
  sendType: 'all',
  userIds: [],
  type: 1,
  title: '',
  content: ''
})
const sendRules = {
  sendType: [{ required: true, message: '请选择发送对象', trigger: 'change' }],
  userIds: [{ required: true, message: '请选择用户', trigger: 'change' }],
  type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
}

// 加载通知列表
const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await adminGetNotificationList(queryParams)
    if (res.data) {
      notifications.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载通知列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await adminGetStats()
    if (res.data) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载用户列表
const loadUserList = async () => {
  try {
    const res = await pageUsers({ page: 1, size: 1000 })
    if (res.data) {
      userList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

// 获取类型名称
const getTypeName = (type) => {
  const map = { 1: '系统通知', 2: '报表通知', 3: '任务通知' }
  return map[type] || '未知'
}

// 获取类型标签类型
const getTypeTagType = (type) => {
  const map = { 1: 'primary', 2: 'success', 3: 'warning' }
  return map[type] || 'info'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  loadNotifications()
}

// 重置
const handleReset = () => {
  queryParams.type = null
  queryParams.isRead = null
  queryParams.page = 1
  loadNotifications()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 分页
const handleSizeChange = () => {
  queryParams.page = 1
  loadNotifications()
}

const handleCurrentChange = () => {
  loadNotifications()
}

// 发送通知
const handleSendNotification = () => {
  sendForm.sendType = 'all'
  sendForm.userIds = []
  sendForm.type = 1
  sendForm.title = ''
  sendForm.content = ''
  sendDialogVisible.value = true
}

// 提交发送
const handleSendSubmit = async () => {
  try {
    await sendFormRef.value.validate()
    sendLoading.value = true

    const data = {
      title: sendForm.title,
      content: sendForm.content,
      type: sendForm.type
    }

    if (sendForm.sendType === 'all') {
      await adminSendToAll(data)
    } else {
      data.userIds = sendForm.userIds
      await adminSendNotification(data)
    }

    ElMessage.success('发送成功')
    sendDialogVisible.value = false
    loadNotifications()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发送失败:', error)
    }
  } finally {
    sendLoading.value = false
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminDeleteNotification(row.id)
    ElMessage.success('删除成功')
    loadNotifications()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条通知吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminBatchDeleteNotifications(selectedIds.value)
    ElMessage.success('删除成功')
    selectedIds.value = []
    loadNotifications()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadNotifications()
  loadStats()
  loadUserList()
})
</script>

<style lang="scss" scoped>
.message-manage-container {
  padding: 0;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: $bg-primary;
  border-radius: $radius-lg;
  box-shadow: $shadow-sm;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;

  &.total {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
  }

  &.unread {
    background: rgba($warning-color, 0.1);
    color: $warning-color;
  }

  &.today {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: $text-secondary;
  margin-top: 4px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: $bg-primary;
  border-radius: $radius-lg;
}

.action-left,
.action-right {
  display: flex;
  gap: 12px;
}

.message-table {
  background: $bg-primary;
  border-radius: $radius-lg;

  :deep(.el-table__header) {
    th {
      background: $gray-50;
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 16px;
  background: $bg-primary;
  border-radius: $radius-lg;
}
</style>
