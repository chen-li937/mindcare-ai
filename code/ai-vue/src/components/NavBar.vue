<template>
  <div class="navbar">
    <div class="flex-box">
      <el-button text @click="$emit('toggle')">
        <el-icon :size="20"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
      </el-button>
      <p class="page-title">{{ pageTitle }}</p>
    </div>
    <div class="flex-box">
      <el-tooltip content="全屏显示" placement="bottom">
        <el-button text class="icon-btn" @click="toggleFullscreen">
          <el-icon :size="18"><FullScreen /></el-icon>
        </el-button>
      </el-tooltip>
      <el-button type="primary" class="chat-entry" @click="router.push('/chat')">
        <el-icon><ChatDotRound /></el-icon>AI 心理咨询
      </el-button>
      <el-dropdown @command="handleCommand">
        <div class="flex-box user-box">
          <el-avatar :size="32" class="user-avatar">
            {{ avatarText }}
          </el-avatar>
          <p class="user-name">{{ displayName }}</p>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile" :icon="UserFilled">个人中心</el-dropdown-item>
            <el-dropdown-item command="logout" :icon="SwitchButton" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, SwitchButton, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

defineProps({
  collapsed: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['toggle'])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageTitle = computed(() => route.meta?.title || '心理健康AI助手')
const displayName = computed(() => userStore.userName || 'admin')
const avatarText = computed(() => displayName.value.charAt(0).toUpperCase())

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen?.()
  } else {
    document.exitFullscreen?.()
  }
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning',
      })
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.replace('/login')
    } catch {
      // 用户取消
    }
  } else if (command === 'profile') {
    ElMessage.info('个人中心待接入（演示）')
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  border-bottom: 1px solid #e5e7ea;

  .flex-box {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .icon-btn {
    margin-right: 8px;
    color: #606266;
  }

  .chat-entry {
    margin-right: 12px;
  }

  .user-box {
    cursor: pointer;
    gap: 8px;
    padding: 0 6px;

    .user-avatar {
      background: linear-gradient(135deg, #2bb3a3, #3a8ee6);
      color: #fff;
      font-size: 14px;
    }

    .user-name {
      margin: 0;
      font-size: 14px;
      color: #303133;
    }
  }

  .page-title {
    margin-left: 12px;
    font-size: 18px;
    font-weight: bold;
    color: #1f2937;
  }
}
</style>
