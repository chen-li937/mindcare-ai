import request from '@/utils/request'
import { mockGetMoodLogs, mockGetMoodStats } from '@/mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

/**
 * 情绪日志分页列表
 * 真实接口：GET /api/emotion-diary/page?keyword=&mood=&page=&pageSize=
 * 返回：{ list, total }，list 项：{ id, user, mood, score(1-5), emotionTags, content, aiReply, weather, sleepHours, logDate, createdAt }
 */
export function getMoodLogs(params) {
  if (USE_MOCK) return mockGetMoodLogs(params)
  return request.get('/emotion-diary/page', { params }).then((res) => res.data)
}

/**
 * 情绪统计（分布 + 平均评分）
 * 真实接口：GET /api/emotion-diary/stats
 * 返回：{ total, distribution: [{name, count}], avgScore(1-5) }
 */
export function getMoodStats() {
  if (USE_MOCK) return mockGetMoodStats()
  return request.get('/emotion-diary/stats').then((res) => res.data)
}
