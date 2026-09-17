import request from '@/utils/request'
import { getToken, removeToken, removeStoredUser } from '@/utils/auth'
import router from '@/router'

/**
 * 用户端 AI 心理咨询 API
 *
 * 后端接口：
 * - POST /psychological-chat/session/start  创建会话（保存初始消息）
 * - POST /psychological-chat/stream         SSE 流式对话（text/event-stream）
 */

// 创建咨询会话，返回 { sessionId, initialMessage, expireTime, ... }
export function startSession(initialMessage, sessionTitle) {
  return request.post('/psychological-chat/session/start', {
    initialMessage,
    sessionTitle: sessionTitle || initialMessage.slice(0, 50),
  })
}

/**
 * 流式对话（SSE）
 * EventSource 不支持 POST/自定义头，这里用 fetch + ReadableStream 手动解析
 *
 * @param {Object} params
 * @param {string} params.sessionId  会话ID（session_xxx）
 * @param {string} params.userMessage 用户消息
 * @param {(fragment: string) => void} onFragment 收到 AI 文本片段时的回调
 * @param {AbortSignal} [signal] 中断信号
 */
export async function streamChat({ sessionId, userMessage, onFragment, signal }) {
  let response
  try {
    response = await fetch('/api/psychological-chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        token: getToken() || '',
      },
      body: JSON.stringify({ sessionId, userMessage }),
      signal,
    })
  } catch (err) {
    if (err.name === 'AbortError') return
    throw new Error('网络异常，请检查后端服务是否启动')
  }

  if (response.status === 401) {
    removeToken()
    removeStoredUser()
    router.replace({ path: '/login', query: { redirect: '/chat' } })
    throw new Error('登录状态已过期，请重新登录')
  }
  if (!response.ok || !response.body) {
    throw new Error('连接 AI 服务失败，请稍后重试')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  // SSE 按空行分帧，每帧内 event:/data: 行
  const handleFrame = (frame) => {
    const lines = frame.split('\n')
    let event = 'message'
    let data = ''
    for (const line of lines) {
      if (line.startsWith('event:')) {
        event = line.slice(6).trim()
      } else if (line.startsWith('data:')) {
        data += line.slice(5).trim()
      }
    }
    if (!data) return

    // 后端约定：message 事件 data 为 {"content":"片段","type":"normal"}，结束时发 {}
    let payload
    try {
      payload = JSON.parse(data)
    } catch {
      return
    }
    if (event === 'error') {
      throw new Error(payload.msg || payload.data || 'AI 服务异常')
    }
    if (payload && payload.content) {
      onFragment(payload.content)
    }
  }

  try {
    for (;;) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      let idx
      while ((idx = buffer.indexOf('\n\n')) !== -1) {
        const frame = buffer.slice(0, idx)
        buffer = buffer.slice(idx + 2)
        handleFrame(frame)
      }
    }
    // 处理剩余未分帧内容
    if (buffer.trim()) {
      handleFrame(buffer)
    }
  } catch (err) {
    if (err.name === 'AbortError') return
    throw err
  }
}
