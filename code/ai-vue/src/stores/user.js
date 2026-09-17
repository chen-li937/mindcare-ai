import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo, logout as logoutApi } from '@/api/auth'
import {
  getToken,
  setToken,
  removeToken,
  getStoredUser,
  setStoredUser,
  removeStoredUser,
} from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getStoredUser(),
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    userName: (state) =>
      state.userInfo?.displayName || state.userInfo?.nickname || state.userInfo?.username || '',
    roles: (state) =>
      state.userInfo?.userType ? [state.userInfo.userType] : state.userInfo?.roles || [],
  },

  actions: {
    /** 登录：换取 token + 用户信息 */
    async login(loginForm) {
      const data = await loginApi(loginForm)
      this.token = data.token
      setToken(data.token)
      if (data.userInfo) {
        this.userInfo = data.userInfo
        setStoredUser(data.userInfo)
      }
      return data
    },

    /** 拉取当前用户信息（刷新页面后恢复登录态） */
    async fetchUserInfo() {
      const data = await getUserInfo()
      this.userInfo = data
      setStoredUser(data)
      return data
    },

    /** 退出登录：通知后端 + 清理本地状态 */
    async logout() {
      await logoutApi()
      this.reset()
    },

    reset() {
      this.token = ''
      this.userInfo = null
      removeToken()
      removeStoredUser()
    },
  },
})
