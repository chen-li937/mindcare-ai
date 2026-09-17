<template>
  <div class="chat-page">
    <!-- 顶栏 -->
    <header class="chat-header">
      <div class="header-left">
        <el-button text class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>返回后台
        </el-button>
      </div>
      <div class="header-center">
        <el-image :src="logoUrl" class="header-logo" />
        <div>
          <p class="header-title">AI 心理咨询</p>
          <p class="header-sub">
            <span class="status-dot"></span>AI 疏导师在线 · 对话内容将匿名保存
          </p>
        </div>
      </div>
      <div class="header-right">
        <el-button :disabled="streaming" @click="resetSession">
          <el-icon><RefreshLeft /></el-icon>新会话
        </el-button>
      </div>
    </header>

    <!-- 消息区 -->
    <main ref="scrollRef" class="chat-body">
      <div class="chat-container">
        <!-- 空状态：欢迎语 + 快捷提问 -->
        <template v-if="messages.length === 0">
          <div class="welcome">
            <el-image :src="logoUrl" class="welcome-logo" />
            <h2>你好，我是你的 AI 心理疏导师</h2>
            <p>温和、耐心、完全保密。任何情绪都值得被倾听，从一句话开始吧</p>
            <div class="quick-prompts">
              <button
                v-for="q in quickPrompts"
                :key="q"
                class="quick-item"
                :disabled="streaming"
                @click="sendText(q)"
              >
                <el-icon><ChatDotRound /></el-icon>{{ q }}
              </button>
            </div>
          </div>
        </template>

        <!-- 消息列表 -->
        <div
          v-for="(msg, i) in messages"
          :key="i"
          class="msg-row"
          :class="msg.from"
        >
          <el-avatar v-if="msg.from === 'ai'" :size="36" class="ai-avatar">
            <el-image :src="logoUrl" class="ai-avatar-img" />
          </el-avatar>
          <div class="bubble" :class="{ streaming: msg.streaming }">
            <span class="bubble-text">{{ msg.text }}</span>
            <span v-if="msg.streaming && !msg.text" class="typing-dots">
              <i></i><i></i><i></i>
            </span>
          </div>
        </div>
      </div>
    </main>

    <!-- 输入区 -->
    <footer class="chat-footer">
      <div class="input-wrap">
        <el-input
          v-model="draft"
          type="textarea"
          :autosize="{ minRows: 1, maxRows: 5 }"
          maxlength="2000"
          resize="none"
          placeholder="说说你最近的感受…（Enter 发送，Shift+Enter 换行）"
          @keydown.enter.exact.prevent="handleEnter"
        />
        <el-button
          type="primary"
          class="send-btn"
          :loading="streaming"
          :disabled="!draft.trim() && !streaming"
          @click="handleSend"
        >
          <el-icon v-if="!streaming"><Promotion /></el-icon>
          {{ streaming ? '回复中' : '发送' }}
        </el-button>
      </div>
      <p class="footer-tip">本服务不能替代专业心理治疗，如遇紧急情况请拨打心理援助热线 12356</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, RefreshLeft, ChatDotRound, Promotion } from '@element-plus/icons-vue'
import { startSession, streamChat } from '@/api/chat'
import logoUrl from '@/assets/images/logo.jpg'

const router = useRouter()

const quickPrompts = [
  '我最近总是失眠，躺在床上会想很多事',
  '期末临近，感觉压力好大，很焦虑',
  '心情持续低落，对什么都提不起兴趣',
]

const messages = ref([]) // [{ from: 'user'|'ai', text, time, streaming }]
const draft = ref('')
const streaming = ref(false)
const sessionId = ref('')
const scrollRef = ref(null)
let abortController = null

const goBack = () => router.replace('/back/dashboard')

const scrollToBottom = () => {
  nextTick(() => {
    const el = scrollRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

const now = () => {
  const d = new Date()
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const handleEnter = () => {
  if (!streaming.value) handleSend()
}

const handleSend = () => {
  const text = draft.value.trim()
  if (!text || streaming.value) return
  draft.value = ''
  sendText(text)
}

// 发送一条消息：首条先建会话，再走 SSE 流式拿 AI 回复
const sendText = async (text) => {
  messages.value.push({ from: 'user', text, time: now() })
  // 必须 reactive() 包裹：闭包里直接改普通对象不会触发视图更新（Proxy 陷阱）
  const aiMsg = reactive({ from: 'ai', text: '', time: now(), streaming: true })
  messages.value.push(aiMsg)
  streaming.value = true
  scrollToBottom()

  abortController = new AbortController()

  try {
    // 首条消息先创建会话（后端会保存初始消息）
    if (!sessionId.value) {
      const res = await startSession(text)
      sessionId.value = res.data?.sessionId
      if (!sessionId.value) throw new Error('创建会话失败')
    }

    await streamChat({
      sessionId: sessionId.value,
      userMessage: text,
      signal: abortController.signal,
      onFragment: (fragment) => {
        aiMsg.text += fragment
        scrollToBottom()
      },
    })

    // 流结束但没有任何回复内容
    if (!aiMsg.text) {
      aiMsg.text = '（AI 没有返回内容，请重试）'
    }
  } catch (err) {
    if (aiMsg.text) {
      aiMsg.text += '\n（回复中断，可点击「新会话」重新开始）'
    } else {
      aiMsg.text = `抱歉，出了点问题：${err.message || '未知错误'}`
    }
    ElMessage.error(err.message || '发送失败')
  } finally {
    aiMsg.streaming = false
    aiMsg.time = now()
    streaming.value = false
    abortController = null
    scrollToBottom()
  }
}

// 新会话：中断当前流，清空状态
const resetSession = () => {
  abortController?.abort()
  sessionId.value = ''
  messages.value = []
  streaming.value = false
}

onUnmounted(() => abortController?.abort())
</script>

<style lang="scss" scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f5f3;
}

/* 顶栏 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e5e7ea;

  .header-left .back-btn {
    color: #606266;
  }

  .header-center {
    display: flex;
    align-items: center;
    gap: 12px;

    .header-logo {
      width: 40px;
      height: 40px;
      border-radius: 10px;
    }

    .header-title {
      margin: 0;
      font-size: 17px;
      font-weight: bold;
      color: #1f2937;
    }

    .header-sub {
      display: flex;
      align-items: center;
      gap: 6px;
      margin: 2px 0 0;
      font-size: 12px;
      color: #909399;

      .status-dot {
        width: 7px;
        height: 7px;
        border-radius: 50%;
        background: #67c23a;
      }
    }
  }
}

/* 消息区 */
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px 16px;

  .chat-container {
    max-width: 860px;
    margin: 0 auto;
  }
}

/* 欢迎态 */
.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 8vh;
  text-align: center;

  .welcome-logo {
    width: 64px;
    height: 64px;
    border-radius: 16px;
  }

  h2 {
    margin: 18px 0 8px;
    font-size: 22px;
    color: #1f2937;
  }

  p {
    margin: 0 0 32px;
    font-size: 14px;
    color: #909399;
  }

  .quick-prompts {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;

    .quick-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 16px;
      font-size: 13px;
      color: #2bb3a3;
      cursor: pointer;
      background: #fff;
      border: 1px solid #d8e6e3;
      border-radius: 10px;
      transition: all 0.2s;

      &:hover:not(:disabled) {
        border-color: #2bb3a3;
        box-shadow: 0 2px 10px rgba(43, 179, 163, 0.15);
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }
}

/* 消息行 */
.msg-row {
  display: flex;
  margin-bottom: 18px;
  gap: 10px;

  &.user {
    justify-content: flex-end;

    .bubble {
      color: #fff;
      background: linear-gradient(135deg, #2bb3a3 0%, #3a8ee6 100%);
      border-radius: 14px 14px 4px 14px;
    }
  }

  &.ai {
    .bubble {
      background: #fff;
      border: 1px solid #e8ecea;
      border-radius: 14px 14px 14px 4px;
    }
  }

  .ai-avatar {
    flex-shrink: 0;
    background: #fff;

    .ai-avatar-img {
      width: 100%;
      height: 100%;
      border-radius: 50%;
    }
  }

  .bubble {
    max-width: 72%;
    padding: 10px 14px;
    box-shadow: 0 1px 3px rgba(0, 21, 41, 0.05);

    .bubble-text {
      font-size: 14px;
      line-height: 1.7;
      white-space: pre-wrap;
      word-break: break-word;
    }
  }
}

/* 等待首片段的打字动画 */
.typing-dots {
  display: inline-flex;
  gap: 4px;
  padding: 4px 0;

  i {
    width: 6px;
    height: 6px;
    background: #b8c4c1;
    border-radius: 50%;
    animation: blink 1.2s infinite;

    &:nth-child(2) {
      animation-delay: 0.2s;
    }
    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

@keyframes blink {
  0%,
  80%,
  100% {
    opacity: 0.3;
  }
  40% {
    opacity: 1;
  }
}

/* 输入区 */
.chat-footer {
  padding: 12px 16px 16px;
  background: #f0f5f3;

  .input-wrap {
    display: flex;
    align-items: flex-end;
    gap: 12px;
    max-width: 860px;
    margin: 0 auto;
    padding: 10px 12px;
    background: #fff;
    border: 1px solid #e5e7ea;
    border-radius: 14px;
    box-shadow: 0 2px 10px rgba(0, 21, 41, 0.04);

    :deep(.el-textarea__inner) {
      padding: 6px 4px;
      font-size: 14px;
      box-shadow: none;
    }

    .send-btn {
      flex-shrink: 0;
    }
  }

  .footer-tip {
    max-width: 860px;
    margin: 8px auto 0;
    font-size: 12px;
    color: #a0a6ad;
    text-align: center;
  }
}
</style>
