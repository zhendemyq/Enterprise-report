import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

/**
 * 角色权限说明：
 * - ROLE_ADMIN: 系统管理员，拥有所有权限
 * - ROLE_REPORT_MANAGER: 报表设计师，管理模板、数据源、分类
 * - ROLE_REPORT_USER: 业务用户，生成报表、查看记录、定时任务
 * - 其他角色: 仅首页和基础功能
 */

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
        // 首页所有登录用户都可访问，不设置roles
      }
    ]
  },
  {
    path: '/report',
    component: () => import('@/layout/index.vue'),
    redirect: '/report/generate',
    meta: {
      title: '报表管理',
      icon: 'Document',
      // 报表管理模块：管理员、报表设计师、业务用户都可见
      roles: ['ROLE_ADMIN', 'ROLE_REPORT_MANAGER', 'ROLE_REPORT_USER']
    },
    children: [
      {
        path: 'template',
        name: 'ReportTemplate',
        component: () => import('@/views/report/template/index.vue'),
        meta: {
          title: '模板管理',
          icon: 'Files',
          // 模板管理：仅管理员和报表设计师
          roles: ['ROLE_ADMIN', 'ROLE_REPORT_MANAGER']
        }
      },
      {
        path: 'template/design/:id?',
        name: 'ReportTemplateDesign',
        component: () => import('@/views/report/template/design.vue'),
        meta: {
          title: '模板设计',
          hidden: true,
          // 模板设计：仅管理员和报表设计师
          roles: ['ROLE_ADMIN', 'ROLE_REPORT_MANAGER']
        }
      },
      {
        path: 'generate',
        name: 'ReportGenerate',
        component: () => import('@/views/report/generate/index.vue'),
        meta: {
          title: '报表生成',
          icon: 'Printer',
          // 报表生成：管理员、报表设计师、业务用户
          roles: ['ROLE_ADMIN', 'ROLE_REPORT_MANAGER', 'ROLE_REPORT_USER']
        }
      },
      {
        path: 'records',
        name: 'ReportRecords',
        component: () => import('@/views/report/records/index.vue'),
        meta: {
          title: '生成记录',
          icon: 'List',
          // 生成记录：管理员、报表设计师、业务用户
          roles: ['ROLE_ADMIN', 'ROLE_REPORT_MANAGER', 'ROLE_REPORT_USER']
        }
      },
      {
        path: 'category',
        name: 'ReportCategory',
        component: () => import('@/views/report/category/index.vue'),
        meta: {
          title: '分类管理',
          icon: 'Folder',
          // 分类管理：仅管理员和报表设计师
          roles: ['ROLE_ADMIN', 'ROLE_REPORT_MANAGER']
        }
      }
    ]
  },
  {
    path: '/datasource',
    component: () => import('@/layout/index.vue'),
    redirect: '/datasource/list',
    meta: {
      title: '数据源',
      icon: 'Connection',
      // 数据源管理：仅管理员和报表设计师
      roles: ['ROLE_ADMIN', 'ROLE_REPORT_MANAGER']
    },
    children: [
      {
        path: 'list',
        name: 'DatasourceList',
        component: () => import('@/views/datasource/index.vue'),
        meta: {
          title: '数据源管理',
          icon: 'Coin',
          roles: ['ROLE_ADMIN', 'ROLE_REPORT_MANAGER']
        }
      }
    ]
  },
  {
    path: '/schedule',
    component: () => import('@/layout/index.vue'),
    redirect: '/schedule/list',
    meta: {
      title: '定时任务',
      icon: 'Timer',
      // 定时任务：管理员、报表设计师、业务用户
      roles: ['ROLE_ADMIN', 'ROLE_REPORT_MANAGER', 'ROLE_REPORT_USER']
    },
    children: [
      {
        path: 'list',
        name: 'ScheduleList',
        component: () => import('@/views/schedule/index.vue'),
        meta: {
          title: '任务管理',
          icon: 'Clock',
          roles: ['ROLE_ADMIN', 'ROLE_REPORT_MANAGER', 'ROLE_REPORT_USER']
        }
      }
    ]
  },
  {
    path: '/system',
    component: () => import('@/layout/index.vue'),
    redirect: '/system/user',
    meta: {
      title: '系统管理',
      icon: 'Setting',
      // 系统管理：仅管理员
      roles: ['ROLE_ADMIN']
    },
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
          roles: ['ROLE_ADMIN']
        }
      },
      {
        path: 'role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: {
          title: '角色管理',
          icon: 'UserFilled',
          roles: ['ROLE_ADMIN']
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hidden: true }
  }
]

/**
 * 检查用户是否有权限访问路由
 * @param {Array} userRoles 用户角色列表
 * @param {Object} route 路由对象
 * @returns {boolean}
 */
export function hasPermission(userRoles, route) {
  if (!route.meta?.roles) {
    // 没有设置roles的路由，所有登录用户都可访问
    return true
  }
  // 检查用户角色是否与路由要求的角色有交集
  return userRoles.some(role => route.meta.roles.includes(role))
}

/**
 * 根据用户角色过滤路由
 * @param {Array} routes 路由列表
 * @param {Array} userRoles 用户角色列表
 * @returns {Array}
 */
export function filterRoutesByRoles(routes, userRoles) {
  const result = []
  routes.forEach(route => {
    if (hasPermission(userRoles, route)) {
      const tmp = { ...route }
      if (tmp.children) {
        tmp.children = filterRoutesByRoles(tmp.children, userRoles)
      }
      result.push(tmp)
    }
  })
  return result
}

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 白名单路由
const whiteList = ['/login']

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  document.title = to.meta.title ? `${to.meta.title} - 企业报表系统` : '企业报表系统'

  const userStore = useUserStore()
  const token = userStore.token

  if (token) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      // 如果有token但没有用户信息，先获取用户信息（包含角色）
      if (!userStore.userInfo) {
        try {
          await userStore.fetchUserInfo()
        } catch (error) {
          // 获取用户信息失败，跳转到登录页
          next(`/login?redirect=${to.path}`)
          return
        }
      }
      // 检查路由权限
      const userRoles = userStore.roles || []
      if (hasPermission(userRoles, to)) {
        next()
      } else {
        // 无权限，跳转到首页
        next({ path: '/dashboard' })
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
