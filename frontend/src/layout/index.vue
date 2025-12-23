<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside 
      class="layout-sidebar"
      :class="{ 'is-collapsed': !appStore.sidebarOpened }"
    >
      <div class="sidebar-header">
        <div class="logo-container">
          <div class="logo-icon">
            <el-icon :size="28"><DataAnalysis /></el-icon>
          </div>
          <transition name="fade">
            <span v-show="appStore.sidebarOpened" class="logo-text">企业报表系统</span>
          </transition>
        </div>
      </div>
      
      <el-scrollbar class="sidebar-menu-container">
        <el-menu
          :default-active="activeMenu"
          :collapse="!appStore.sidebarOpened"
          :collapse-transition="false"
          router
          class="sidebar-menu"
        >
          <template v-for="route in menuRoutes" :key="route.path">
            <!-- 单个菜单项 -->
            <el-menu-item 
              v-if="!route.children || route.children.length === 1"
              :index="getMenuIndex(route)"
              class="menu-item"
            >
              <el-icon v-if="getMenuIcon(route)">
                <component :is="getMenuIcon(route)" />
              </el-icon>
              <template #title>
                <span>{{ getMenuTitle(route) }}</span>
              </template>
            </el-menu-item>
            
            <!-- 子菜单 -->
            <el-sub-menu 
              v-else 
              :index="route.path"
              class="sub-menu"
            >
              <template #title>
                <el-icon v-if="route.meta?.icon">
                  <component :is="route.meta.icon" />
                </el-icon>
                <span>{{ route.meta?.title }}</span>
              </template>
              <el-menu-item
                v-for="child in filterHidden(route.children)"
                :key="child.path"
                :index="route.path + '/' + child.path"
                class="menu-item"
              >
                <el-icon v-if="child.meta?.icon">
                  <component :is="child.meta.icon" />
                </el-icon>
                <template #title>
                  <span>{{ child.meta?.title }}</span>
                </template>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-scrollbar>
      
      <!-- 侧边栏底部 -->
      <div class="sidebar-footer">
        <div class="collapse-btn" @click="toggleSidebar">
          <el-icon :size="18">
            <Fold v-if="appStore.sidebarOpened" />
            <Expand v-else />
          </el-icon>
        </div>
      </div>
    </aside>
    
    <!-- 主内容区 -->
    <div class="layout-main">
      <!-- 顶部导航 -->
      <header class="layout-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 通知 -->
          <el-popover
            placement="bottom"
            :width="360"
            trigger="click"
            popper-class="notification-popover"
            @show="loadNotifications"
          >
            <template #reference>
              <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="notification-badge">
                <el-button class="header-icon-btn" circle>
                  <el-icon><Bell /></el-icon>
                </el-button>
              </el-badge>
            </template>
            <div class="notification-panel">
              <div class="notification-header">
                <span class="notification-title">通知</span>
                <el-button v-if="unreadCount > 0" type="primary" link size="small" @click="handleMarkAllRead">
                  全部已读
                </el-button>
              </div>
              <div class="notification-list" v-loading="notificationLoading">
                <template v-if="notifications.length > 0">
                  <div
                    v-for="item in notifications"
                    :key="item.id"
                    class="notification-item"
                    :class="{ 'is-unread': item.isRead === 0 }"
                    @click="handleNotificationClick(item)"
                  >
                    <div class="notification-icon" :class="getNotificationTypeClass(item.type)">
                      <el-icon><component :is="getNotificationIcon(item.type)" /></el-icon>
                    </div>
                    <div class="notification-content">
                      <div class="notification-item-title">{{ item.title }}</div>
                      <div class="notification-item-desc">{{ item.content }}</div>
                      <div class="notification-time">{{ formatTime(item.createTime) }}</div>
                    </div>
                  </div>
                </template>
                <el-empty v-else description="暂无通知" :image-size="80" />
              </div>
              <div class="notification-footer" v-if="notifications.length > 0">
                <el-button type="primary" link @click="goToNotificationCenter">查看全部</el-button>
              </div>
            </div>
          </el-popover>
          
          <!-- 用户信息 -->
          <el-dropdown trigger="click" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                {{ userStore.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.nickname || userStore.username }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item>
                  <el-icon><Setting /></el-icon>
                  <span>系统设置</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 内容区域 -->
      <main class="layout-content">
        <router-view v-slot="{ Component, route }">
          <transition name="fade" mode="out-in">
            <keep-alive :include="cachedViews">
              <component :is="Component" :key="route.path" />
            </keep-alive>
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { getRecentNotifications, markAsRead, markAllAsRead } from '@/api/notification'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const cachedViews = ref(['Dashboard'])

// 通知相关
const notifications = ref([])
const unreadCount = ref(0)
const notificationLoading = ref(false)

// 加载通知
const loadNotifications = async () => {
  notificationLoading.value = true
  try {
    const res = await getRecentNotifications(5)
    if (res.data) {
      notifications.value = res.data.list || []
      unreadCount.value = res.data.unreadCount || 0
    }
  } catch (error) {
    console.error('加载通知失败:', error)
  } finally {
    notificationLoading.value = false
  }
}

// 获取通知类型样式
const getNotificationTypeClass = (type) => {
  const map = { 1: 'system', 2: 'report', 3: 'task' }
  return map[type] || 'system'
}

// 获取通知图标
const getNotificationIcon = (type) => {
  const map = { 1: 'Bell', 2: 'Document', 3: 'Timer' }
  return map[type] || 'Bell'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const now = dayjs()
  const target = dayjs(time)
  const diff = now.diff(target, 'minute')
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff}分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`
  return target.format('MM-DD HH:mm')
}

// 点击通知
const handleNotificationClick = async (item) => {
  if (item.isRead === 0) {
    await markAsRead(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

// 全部已读
const handleMarkAllRead = async () => {
  await markAllAsRead()
  notifications.value.forEach(item => item.isRead = 1)
  unreadCount.value = 0
}

// 跳转通知中心
const goToNotificationCenter = () => {
  router.push('/notification')
}

// 初始化加载未读数量
onMounted(() => {
  loadNotifications()
})

/**
 * 检查用户是否有权限访问路由
 */
const hasPermission = (route) => {
  const userRoles = userStore.roles || []
  if (!route.meta?.roles) {
    return true
  }
  return userRoles.some(role => route.meta.roles.includes(role))
}

/**
 * 递归过滤路由，根据用户角色
 */
const filterRoutes = (routes) => {
  const result = []
  routes.forEach(route => {
    if (hasPermission(route)) {
      const tmp = { ...route }
      if (tmp.children) {
        tmp.children = filterRoutes(tmp.children)
        // 如果子路由全部被过滤掉，则不显示父菜单
        if (tmp.children.length === 0 && route.children?.length > 0) {
          return
        }
      }
      result.push(tmp)
    }
  })
  return result
}

// 获取菜单路由（根据用户角色过滤）
const menuRoutes = computed(() => {
  const routes = router.options.routes
  const filteredRoutes = routes.filter(r => !r.meta?.hidden && r.path !== '/login' && r.path !== '/:pathMatch(.*)*')
  // 根据用户角色过滤路由
  return filterRoutes(filteredRoutes)
})

// 当前激活菜单
const activeMenu = computed(() => {
  const { path, meta } = route
  if (meta?.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 面包屑
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.slice(1) // 去掉首页
})

// 过滤隐藏菜单
const filterHidden = (routes) => {
  return routes?.filter(r => !r.meta?.hidden) || []
}

// 获取菜单图标
const getMenuIcon = (route) => {
  if (route.children?.length === 1) {
    return route.children[0].meta?.icon || route.meta?.icon
  }
  return route.meta?.icon
}

// 获取菜单标题
const getMenuTitle = (route) => {
  if (route.children?.length === 1) {
    return route.children[0].meta?.title || route.meta?.title
  }
  return route.meta?.title
}

// 获取单菜单项的路由路径
const getMenuIndex = (route) => {
  if (route.children?.length === 1) {
    const basePath = route.path === '/' ? '' : route.path
    return basePath + '/' + route.children[0].path
  }
  return route.path
}

// 切换侧边栏
const toggleSidebar = () => {
  appStore.toggleSidebar()
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.logout()
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  width: 100%;
  height: 100%;
  background: $bg-secondary;
}

// 侧边栏
.layout-sidebar {
  width: $sidebar-width;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: $bg-primary;
  border-right: 1px solid $border-color;
  transition: width $transition-normal;
  
  &.is-collapsed {
    width: $sidebar-collapsed-width;
    
    .sidebar-header {
      padding: 20px 12px;
    }
    
    .logo-icon {
      margin-right: 0;
    }
  }
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid $border-color;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo-icon {
  width: 44px;
  height: 44px;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, $primary-color, $primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 12px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  white-space: nowrap;
}

.sidebar-menu-container {
  flex: 1;
  overflow: hidden;
}

.sidebar-menu {
  border-right: none !important;
  padding: 12px;
  
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 48px;
    line-height: 48px;
    border-radius: $radius-md;
    margin-bottom: 4px;
    color: $text-secondary;
    transition: all $transition-normal;
    
    &:hover {
      background: $gray-50;
      color: $text-primary;
    }
    
    .el-icon {
      font-size: 20px;
      margin-right: 12px;
    }
  }
  
  :deep(.el-menu-item.is-active) {
    background: rgba($primary-color, 0.08);
    color: $primary-color;
    font-weight: $font-weight-medium;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 24px;
      background: $primary-color;
      border-radius: 0 $radius-xs $radius-xs 0;
    }
  }
  
  :deep(.el-sub-menu) {
    .el-sub-menu__title {
      border-radius: $radius-md;
    }
    
    .el-menu--inline {
      padding-left: 12px;
      
      .el-menu-item {
        height: 44px;
        line-height: 44px;
        padding-left: 48px !important;
      }
    }
  }
  
  &.el-menu--collapse {
    padding: 12px 10px;
    
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      padding: 0 !important;
      justify-content: center;
      
      .el-icon {
        margin-right: 0;
      }
    }
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid $border-color;
}

.collapse-btn {
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-md;
  cursor: pointer;
  color: $text-secondary;
  transition: all $transition-fast;
  
  &:hover {
    background: $gray-50;
    color: $text-primary;
  }
}

// 主内容区
.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

// 顶部导航
.layout-header {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: $glass-bg;
  backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid $border-color;
  position: sticky;
  top: 0;
  z-index: $z-sticky;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 240px;
  
  :deep(.el-input__wrapper) {
    background: $gray-50;
    border-radius: $radius-full;
  }
}

.header-icon-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  
  &:hover {
    background: $gray-50;
  }
}

.notification-badge {
  :deep(.el-badge__content) {
    background: $danger-color;
    border: 2px solid $bg-primary;
  }
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: $radius-full;
  transition: all $transition-fast;
  
  &:hover {
    background: $gray-50;
  }
}

.user-avatar {
  background: linear-gradient(135deg, $primary-color, $info-color);
  font-weight: $font-weight-semibold;
}

.user-name {
  margin: 0 8px;
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.arrow-icon {
  color: $text-secondary;
  font-size: 12px;
}

// 内容区域
.layout-content {
  flex: 1;
  padding: 24px;
  overflow: auto;
}

// 通知面板样式
.notification-panel {
  margin: -12px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid $border-color;
}

.notification-title {
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.notification-list {
  max-height: 360px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: $gray-50;
  }

  &.is-unread {
    background: rgba($primary-color, 0.04);

    .notification-item-title {
      font-weight: $font-weight-semibold;
    }
  }
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: $radius-md;
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

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-item-title {
  font-size: 14px;
  color: $text-primary;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notification-item-desc {
  font-size: 12px;
  color: $text-secondary;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-time {
  font-size: 12px;
  color: $text-tertiary;
}

.notification-footer {
  padding: 12px 16px;
  text-align: center;
  border-top: 1px solid $border-color;
}
</style>
