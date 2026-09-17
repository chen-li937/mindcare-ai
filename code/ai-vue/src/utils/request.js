import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken, removeStoredUser } from './auth'
import router from '@/router'

// 统一的 axios 实例：所有后端请求都走这里
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API, // /api
  timeout: 15000,
})

// 请求拦截：自动携带 JWT（后端 JwtAuthticationFilter 读取 token 请求头，无需 Bearer 前缀）
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.token = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：后端统一返回 { code: "200", msg, data }
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 兼容直接返回原始数据（非统一结构）的接口
    if (res === null || typeof res !== 'object' || res.code === undefined) {
      return res
    }

    if (res.code === '200' || res.code === 200 || res.code === 0) {
      return res
    }

    // 业务失败（BusinessException / 参数校验失败等，HTTP 200 但 code 非 200）
    ElMessage.error(res.msg || '请求失败')

    if (res.code === '401' || res.code === 401) {
      handleUnauthorized()
    }
    return Promise.reject(new Error(res.msg || 'Error'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      handleUnauthorized()
      ElMessage.error('登录状态已过期，请重新登录')
    } else {
      const message =
        error.response?.data?.msg ||
        (error.code === 'ECONNABORTED' ? '请求超时，请稍后重试' : '网络异常，请检查后端服务是否启动')
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

function handleUnauthorized() {
  removeToken()
  removeStoredUser()
  const current = router.currentRoute.value.fullPath
  if (!current.startsWith('/login')) {
    router.replace({ path: '/login', query: { redirect: current } })
  }
}

export default service
