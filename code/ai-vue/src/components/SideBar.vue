<template>
  <el-aside :width="collapse ? '64px' : '264px'" class="sidebar">
    <el-menu
      :default-active="activeMenu"
      :collapse="collapse"
      :collapse-transition="false"
      router
      class="menu-style"
    >
      <div class="brand">
        <el-image
          :style="{ width: '48px', height: '48px', flexShrink: '0' }"
          :src="iconUrl"
          alt="logo"
          class="brand-logo"
        />
        <div v-show="!collapse" class="info-card">
          <h1 class="brand-title">心理健康AI助手</h1>
          <p class="brand-subtitle">管理后台</p>
        </div>
      </div>
      <el-menu-item
        v-for="item in menus"
        :key="item.path"
        :index="item.path"
      >
        <el-icon><component :is="item.meta.icon" /></el-icon>
        <template #title>
          <span>{{ item.meta.title }}</span>
        </template>
      </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import iconUrl from '@/assets/images/logo.jpg'

defineProps({
  collapse: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const router = useRouter()

const parentRoute = router.options.routes.find((r) => r.path === '/back')

const menus = computed(() =>
  (parentRoute?.children || []).map((item) => ({
    ...item,
    path: `${parentRoute.path}/${item.path}`
  }))
)

const activeMenu = computed(() => route.path)
</script>

<style lang="scss" scoped>
.sidebar {
  transition: width 0.28s;
  overflow: hidden;
}

.menu-style {
  height: 100%;
  border-right: none;

  .brand {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    height: 64px;
    padding: 0 10px;
    background-color: #fff;
    border-bottom: 1px solid #e5e7ed;
    white-space: nowrap;
    overflow: hidden;

    .info-card {
      .brand-title {
        font-size: 16px;
        font-weight: bold;
        margin: 0;
        color: #303133;
      }

      .brand-subtitle {
        font-size: 12px;
        margin: 2px 0 0;
        color: #909399;
      }
    }
  }

  .el-menu-item {
    span {
      font-size: 14px;
    }
  }
}
</style>
