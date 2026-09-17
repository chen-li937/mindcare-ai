<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-content">
        <el-image :src="logoUrl" class="brand-logo" />
        <h1 class="brand-name">心理健康AI助手</h1>
        <p class="brand-slogan">倾听每一种情绪，守护每一颗心灵</p>
        <ul class="feature-list">
          <li><el-icon><ChatDotRound /></el-icon>7×24 小时 AI 情绪陪伴</li>
          <li><el-icon><DataAnalysis /></el-icon>咨询数据与情绪趋势洞察</li>
          <li><el-icon><Warning /></el-icon>高风险会话智能预警与转介</li>
          <li><el-icon><Reading /></el-icon>专业心理知识库持续更新</li>
        </ul>
      </div>
      <div class="brand-footer">© 2026 心理健康AI助手 · 管理后台</div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="form-panel">
      <div class="login-card">
        <h2 class="login-title">欢迎登录</h2>
        <p class="login-subtitle">请输入管理员账号和密码</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              :type="passwordVisible ? 'text' : 'password'"
            >
              <template #suffix>
                <el-icon class="eye-icon" @click="passwordVisible = !passwordVisible">
                  <View v-if="passwordVisible" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <div class="form-options">
            <el-checkbox v-model="form.remember">记住账号</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>

        <el-alert
          v-if="useMock"
          class="demo-tip"
          type="info"
          :closable="false"
          show-icon
          title="演示模式"
          description="当前为本地 Mock 模式，任意输入无法登录"
        />
        <el-alert v-else class="demo-tip" type="info" :closable="false" show-icon title="测试账号" description="账号 demo / 密码 123456" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, View, Hide } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import logoUrl from '@/assets/images/logo.jpg'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const useMock = import.meta.env.VITE_USE_MOCK === 'true'

const formRef = ref(null)
const loading = ref(false)
const passwordVisible = ref(false)

const REMEMBER_KEY = 'psy_remember_username'

const form = reactive({
  username: '',
  password: '',
  remember: false,
})

onMounted(() => {
  const saved = localStorage.getItem(REMEMBER_KEY)
  if (saved) {
    form.username = saved
    form.remember = true
  }
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' },
  ],
}

const handleLogin = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login({
        username: form.username.trim(),
        password: form.password,
      })
      await userStore.fetchUserInfo()

      if (form.remember) {
        localStorage.setItem(REMEMBER_KEY, form.username.trim())
      } else {
        localStorage.removeItem(REMEMBER_KEY)
      }

      ElMessage.success('登录成功，欢迎回来')
      const redirect = route.query.redirect || '/back/dashboard'
      router.replace(redirect)
    } catch (err) {
      // 错误提示已由请求拦截器 / mock 抛出，这里兜底
      if (err?.message) ElMessage.error(err.message)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.login-page {
  display: flex;
  height: 100vh;
  background-color: #f0f5f3;
}

/* 左侧品牌区 */
.brand-panel {
  flex: 1.2;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  color: #fff;
  background: linear-gradient(135deg, #2bb3a3 0%, #3a8ee6 100%);
  overflow: hidden;

  &::before,
  &::after {
    content: '';
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
  }

  &::before {
    width: 360px;
    height: 360px;
    top: -120px;
    right: -120px;
  }

  &::after {
    width: 280px;
    height: 280px;
    bottom: -100px;
    left: -80px;
  }

  .brand-content {
    position: relative;
    z-index: 1;
    max-width: 420px;
  }

  .brand-logo {
    width: 72px;
    height: 72px;
    border-radius: 16px;
  }

  .brand-name {
    margin: 20px 0 8px;
    font-size: 32px;
    font-weight: bold;
    color: #fff;
  }

  .brand-slogan {
    margin: 0 0 40px;
    font-size: 16px;
    color: rgba(255, 255, 255, 0.85);
  }

  .feature-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 18px;
      font-size: 15px;
      color: rgba(255, 255, 255, 0.92);

      .el-icon {
        font-size: 18px;
      }
    }
  }

  .brand-footer {
    position: absolute;
    bottom: 24px;
    z-index: 1;
    font-size: 12px;
    color: rgba(255, 255, 255, 0.6);
  }
}

/* 右侧表单区 */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 380px;

  .login-title {
    margin: 0 0 8px;
    font-size: 26px;
    font-weight: bold;
    color: #1f2d3d;
  }

  .login-subtitle {
    margin: 0 0 32px;
    font-size: 14px;
    color: #909399;
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .login-btn {
    width: 100%;
    height: 42px;
    font-size: 16px;
    letter-spacing: 4px;
  }

  .eye-icon {
    cursor: pointer;
    color: #909399;
  }

  .demo-tip {
    margin-top: 20px;
  }
}

@media (max-width: 900px) {
  .brand-panel {
    display: none;
  }
}
</style>
