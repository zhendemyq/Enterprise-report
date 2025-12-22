import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

/**
 * 角色权限说明：
 * - ADMIN: 系统管理员，拥有所有权限
 * - REPORT_MANAGER: 报表设计师，管理模板、数据源、分类
 * - REPORT_USER: 业务用户，生成报表、查看记录、定时任务
 * - 部门主管角色（finance_manager, hr_manager, sales_manager, warehouse_manager, dept_manager）：业务用户权限
 * - report_viewer: 报表查看员，只能查看报表
 * - data_analyst: 数据分析师，业务用户权限
 */

// 业务用户角色列表（可以生成报表、查看记录、设置定时任务）
const BUSINESS_USER_ROLES = [
  'ADMIN',
  'REPORT_MANAGER',
  'REPORT_USER',
  'dept_manager',
  'finance_manager',
  'hr_manager',
  'sales_manager',
  'warehouse_manager',
  'data_analyst',
  'report_viewer'
]

// 报表设计师角色列表（可以管理模板、数据源、分类）
const DESIGNER_ROLES = ['ADMIN', 'REPORT_MANAGER']

// 管理员角色列表
const ADMIN_ROLES = ['ADMIN']

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
      // 报表管理模块：所有业务用户都可见
      roles: BUSINESS_USER_ROLES
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
          roles: DESIGNER_ROLES
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
          roles: DESIGNER_ROLES
        }
      },
      {
        path: 'generate',
        name: 'ReportGenerate',
        component: () => import('@/views/report/generate/index.vue'),
        meta: {
          title: '报表生成',
          icon: 'Printer',
          // 报表生成：所有业务用户
          roles: BUSINESS_USER_ROLES
        }
      },
      {
        path: 'records',
        name: 'ReportRecords',
        component: () => import('@/views/report/records/index.vue'),
        meta: {
          title: '生成记录',
          icon: 'List',
          // 生成记录：所有业务用户
          roles: BUSINESS_USER_ROLES
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
          roles: DESIGNER_ROLES
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
      roles: DESIGNER_ROLES
    },
    children: [
      {
        path: 'list',
        name: 'DatasourceList',
        component: () => import('@/views/datasource/index.vue'),
        meta: {
          title: '数据源管理',
          icon: 'Coin',
          roles: DESIGNER_ROLES
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
      // 定时任务：所有业务用户
      roles: BUSINESS_USER_ROLES
    },
    children: [
      {
        path: 'list',
        name: 'ScheduleList',
        component: () => import('@/views/schedule/index.vue'),
        meta: {
          title: '任务管理',
          icon: 'Clock',
          roles: BUSINESS_USER_ROLES
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
      roles: ADMIN_ROLES
    },
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
          roles: ADMIN_ROLES
        }
      },
      {
        path: 'role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: {
          title: '角色管理',
          icon: 'UserFilled',
          roles: ADMIN_ROLES
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
