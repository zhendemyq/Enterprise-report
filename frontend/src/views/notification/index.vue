<template>
  <div class="notification-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">消息中心</h2>
      <div class="header-actions">
        <el-button type="primary" plain @click="handleMarkAllRead" :disabled="unreadCount === 0">
          <el-icon><Check /></el-icon>
          全部已读
        </el-button>
        <el-button plain @click="handleClearRead" :disabled="readCount === 0">
          <el-icon><Delete /></el-icon>
          清空已读
        </el-button>
      </div>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <el-radio-group v-model="filterType" @change="handleFilterChange">
        <el-radio-button label="all">
          全部
          <el-badge :value="totalCount" :max="99" v-if="totalCount > 0" class="tab-badge" />
        </el-radio-button>
        <el-radio-button label="unread">
          未读
          <el-badge :value="unreadCount" :max="99" v-if="unreadCount > 0" class="tab-badge" />
        </el-radio-button>
        <el-radio-button label="read">已读</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 通知列表 -->
    <div class="notification-list" v-loading="loading">
      <template v-if="notifications.length > 0">
        <div
          v-for="item in notifications"
          :key="item.id"
          class="notification-card"
          :class="{ 'is-unread': item.isRead === 0 }"
        >
          <div class="notification-icon" :class="getTypeClass(item.type)">
            <el-icon :size="24"><component :is="getTypeIcon(item.type)" /></el-icon>
          </div>
          <div class="notification-main">
            <div class="notification-header">
              <span class="notification-title">{{ item.title }}</span>
              <span class="notification-type">{{ getTypeName(item.type) }}</span>
            </div>
            <div class="notification-content">{{ item.content }}</div>
            <div class="notification-footer">
              <span class="notification-time">
                <el-icon><Clock /></el-icon>
                {{ formatTime(item.createTime) }}
              </span>
            </div>
          </div>
          <div class="notification-actions">
            <el-button
              v-if="item.isRead === 0"
              type="primary"
              link
              @click="handleMarkRead(item)"
            >
              标为已读
            </el-button>
            <el-button type="danger" link @click="handleDelete(item)">
              删除
            </el-button>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无消息" :image-size="120" />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getNotificationList,
  markAsRead,
  markAllAsRead,
  deleteNotification,
  clearReadNotifications
} from '@/api/notification'
import dayjs from 'dayjs'

// 定义组件名称
defineOptions({
  name: 'NotificationCenter'
})

// 数据
const loading = ref(false)
const notifications = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterType = ref('all')

// 统计数据
const totalCount = ref(0)
const unreadCount = ref(0)
const readCount = computed(() => totalCount.value - unreadCount.value)

// 加载通知列表
const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await getNotificationList(currentPage.value, pageSize.value)
    if (res.data) {
      // 根据筛选类型过滤
      let list = res.data.records || []
      if (filterType.value === 'unread') {
        list = list.filter(item => item.isRead === 0)
      } else if (filterType.value === 'read') {
        list = list.filter(item => item.isRead === 1)
      }
      notifications.value = list
      total.value = res.data.total || 0

      // 计算统计数据
      const allRecords = res.data.records || []
      totalCount.value = allRecords.length
      unreadCount.value = allRecords.filter(item => item.isRead === 0).length
    }
  } catch (error) {
    console.error('加载通知失败:', error)
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

// 获取类型样式
const getTypeClass = (type) => {
  const map = { 1: 'system', 2: 'report', 3: 'task' }
  return map[type] || 'system'
}

// 获取类型图标
const getTypeIcon = (type) => {
  const map = { 1: 'Bell', 2: 'Document', 3: 'Timer' }
  return map[type] || 'Bell'
}

// 获取类型名称
const getTypeName = (type) => {
  const map = { 1: '系统通知', 2: '报表通知', 3: '任务通知' }
  return map[type] || '系统通知'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 筛选变化
const handleFilterChange = () => {
  currentPage.value = 1
  loadNotifications()
}

// 标记单条已读
const handleMarkRead = async (item) => {
  try {
    await markAsRead(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    ElMessage.success('已标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 全部标记已读
const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    notifications.value.forEach(item => item.isRead = 1)
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除通知
const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteNotification(item.id)
    loadNotifications()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 清空已读
const handleClearRead = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有已读通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await clearReadNotifications()
    loadNotifications()
    ElMessage.success('清空成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空失败')
    }
  }
}

// 分页
const handleSizeChange = () => {
  currentPage.value = 1
  loadNotifications()
}

const handleCurrentChange = () => {
  loadNotifications()
}

// 初始化
onMounted(() => {
  loadNotifications()
})
</script>

<style lang="scss" scoped>
.notification-container {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-tabs {
  margin-bottom: 24px;

  :deep(.el-radio-group) {
    .el-radio-button__inner {
      padding: 10px 20px;
      border-radius: $radius-md;
    }
  }

  .tab-badge {
    margin-left: 6px;

    :deep(.el-badge__content) {
      top: -2px;
      right: -6px;
    }
  }
}

.notification-list {
  min-height: 400px;
}

.notification-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: $bg-primary;
  border-radius: $radius-lg;
  margin-bottom: 12px;
  box-shadow: $shadow-sm;
  transition: all $transition-normal;

  &:hover {
    box-shadow: $shadow-md;
  }

  &.is-unread {
    background: linear-gradient(135deg, rgba($primary-color, 0.02), rgba($primary-color, 0.06));
    border-left: 4px solid $primary-color;

    .notification-title {
      font-weight: $font-weight-semibold;
    }
  }
}

.notification-icon {
  width: 48px;
  height: 48px;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.system {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
  }

  &.report {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }

  &.task {
    background: rgba($warning-color, 0.1);
    color: $warning-color;
  }
}

.notification-main {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.notification-title {
  font-size: 16px;
  color: $text-primary;
}

.notification-type {
  font-size: 12px;
  color: $text-secondary;
  padding: 2px 8px;
  background: $gray-100;
  border-radius: $radius-sm;
}

.notification-content {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: 12px;
}

.notification-footer {
  display: flex;
  align-items: center;
}

.notification-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: $text-tertiary;

  .el-icon {
    font-size: 14px;
  }
}

.notification-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px 0;
}
</style>
