import request from '@/utils/request'
import { mockDashboard } from '@/mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

/**
 * 首页数据看板
 * GET /api/dashboard
 * 返回：{ statCards, lineChart, pieChart, recentList }
 */
export function getDashboard() {
  if (USE_MOCK) return mockDashboard()
  return request.get('/dashboard').then((res) => res.data)
}
