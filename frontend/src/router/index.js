import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

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
      }
    ]
  },
  {
    path: '/report',
    component: () => import('@/layout/index.vue'),
    redirect: '/report/template',
    meta: { title: '报表管理', icon: 'Document' },
    children: [
      {
        path: 'template',
        name: 'ReportTemplate',
        component: () => import('@/views/report/template/index.vue'),
        meta: { title: '模板管理', icon: 'Files' }
      },
      {
        path: 'template/design/:id?',
        name: 'ReportTemplateDesign',
        component: () => import('@/views/report/template/design.vue'),
        meta: { title: '模板设计', hidden: true }
      },
      {
        path: 'generate',
        name: 'ReportGenerate',
        component: () => import('@/views/report/generate/index.vue'),
        meta: { title: '报表生成', icon: 'Printer' }
      },
      {
        path: 'records',
        name: 'ReportRecords',
        component: () => import('@/views/report/records/index.vue'),
        meta: { title: '生成记录', icon: 'List' }
      },
      {
        path: 'category',
        name: 'ReportCategory',
        component: () => import('@/views/report/category/index.vue'),
        meta: { title: '分类管理', icon: 'Folder' }
      }
    ]
  },
  {
    path: '/datasource',
    component: () => import('@/layout/index.vue'),
    redirect: '/datasource/list',
    meta: { title: '数据源', icon: 'Connection' },
    children: [
      {
        path: 'list',
        name: 'DatasourceList',
        component: () => import('@/views/datasource/index.vue'),
        meta: { title: '数据源管理', icon: 'Coin' }
      }
    ]
  },
  {
    path: '/schedule',
    component: () => import('@/layout/index.vue'),
    redirect: '/schedule/list',
    meta: { title: '定时任务', icon: 'Timer' },
    children: [
      {
        path: 'list',
        name: 'ScheduleList',
        component: () => import('@/views/schedule/index.vue'),
        meta: { title: '任务管理', icon: 'Clock' }
      }
    ]
  },
  {
    path: '/system',
    component: () => import('@/layout/index.vue'),
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
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
      next()
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
