import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/auth'
import Cookies from 'js-cookie'

const TOKEN_KEY = 'enterprise_report_token'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: Cookies.get(TOKEN_KEY) || '',
    userInfo: null,
    roles: [],
    permissions: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    nickname: (state) => state.userInfo?.nickname || ''
  },

  actions: {
    // 初始化用户
    async initUser() {
      if (this.token && !this.userInfo) {
        await this.fetchUserInfo()
      }
    },

    // 登录
    async login(loginForm) {
      try {
        const res = await login(loginForm)
        this.token = res.data.token
        this.userInfo = res.data.userInfo
        this.roles = res.data.roles || []
        this.permissions = res.data.permissions || []
        Cookies.set(TOKEN_KEY, res.data.token, { expires: 7 })
        return res
      } catch (error) {
        throw error
      }
    },

    // 获取用户信息
    async fetchUserInfo() {
      try {
        const res = await getUserInfo()
        this.userInfo = res.data
        // 从用户信息中提取角色编码
        if (res.data.roles && res.data.roles.length > 0) {
          this.roles = res.data.roles.map(role => role.roleCode)
        }
        return res
      } catch (error) {
        this.logout()
        throw error
      }
    },

    // 登出
    async logout() {
      try {
        await logout()
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        this.resetState()
      }
    },

    // 重置状态
    resetState() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      this.permissions = []
      Cookies.remove(TOKEN_KEY)
    }
  }
})
