import request from '@/utils/request'
import { mockGetConsultations, mockEscalate } from '@/mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

/**
 * 咨询记录分页列表
 * 真实接口：GET /api/consultation/page?keyword=&risk=&channel=&startDate=&endDate=&page=&pageSize=
 * 返回：{ list, total }，list 项：{ id, user, channel, summary, risk, duration, messages, startTime, status }
 */
export function getConsultations(params) {
  if (USE_MOCK) return mockGetConsultations(params)
  const { dateRange, ...rest } = params
  const query = {
    ...rest,
    startDate: dateRange?.[0],
    endDate: dateRange?.[1],
  }
  return request.get('/consultation/page', { params: query }).then((res) => res.data)
}

/**
 * 会话聊天详情（气泡数据）
 * 真实接口：GET /api/consultation/{id}/messages
 * 返回：[{ from: 'user'|'ai', text, time }]
 */
export function getConsultationMessages(id) {
  if (USE_MOCK) {
    return Promise.resolve([])
  }
  return request.get(`/consultation/${id}/messages`).then((res) => res.data)
}

/**
 * 转人工跟进
 * 后端暂无该接口，前端仅做提示，不发起请求
 */
export function escalateConsultation(id) {
  if (USE_MOCK) return mockEscalate(id)
  return Promise.resolve()
}
