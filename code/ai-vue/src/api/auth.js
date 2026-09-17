import request from '@/utils/request'
import { mockLogin, mockUserInfo } from '@/mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

/**
 * 登录
 * 真实接口：POST /api/user/login  body: { username, password }
 * 返回：{ token, userTypeDisplayName, userInfo: { id, username, nickname, displayName, avatar, userType, ... } }
 */
export function login(data) {
  if (USE_MOCK) return mockLogin(data)
  return request.post('/user/login', data).then((res) => res.data)
}

/**
 * 获取当前登录用户信息
 * 真实接口：GET /api/user/current
 * 返回：{ id, username, nickname, displayName, avatar, userType, ... }
 */
export function getUserInfo() {
  if (USE_MOCK) return mockUserInfo()
  return request.get('/user/current').then((res) => res.data)
}

/**
 * 退出登录
 * 后端暂无登出接口，直接清理本地 token 即可
 */
export function logout() {
  return Promise.resolve()
}
