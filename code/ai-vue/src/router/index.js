import { createRouter, createWebHistory } from 'vue-router'
import BackendLayout from '@/components/BackendLayout.vue'

export const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    redirect: '/back/dashboard',
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/chat/index.vue'),
    meta: { title: 'AI 心理咨询', requiresAuth: true },
  },
  {
    path: '/back',
    component: () => BackendLayout,
    redirect: '/back/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard.vue'),
        meta: { title: '数据分析', icon: 'PieChart', requiresAuth: true },
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/knowledge.vue'),
        meta: { title: '知识文章', icon: 'ChatLineSquare', requiresAuth: true },
      },
      {
        path: 'consultation',
        name: 'Consultation',
        component: () => import('@/views/consultation.vue'),
        meta: { title: '咨询记录', icon: 'Message', requiresAuth: true },
      },
      {
        path: 'emotional',
        name: 'Emotional',
        component: () => import('@/views/emotional.vue'),
        meta: { title: '情绪日志', icon: 'User', requiresAuth: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const APP_TITLE = '心理健康AI助手'

// 全局前置守卫：登录态校验
router.beforeEach((to) => {
  // 已登录用户访问登录页，直接进入后台
  const token = localStorage.getItem('psy_token')
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)

  if (to.path === '/login' && token) {
    return { path: '/back/dashboard' }
  }

  if (requiresAuth && !token) {
    return {
      path: '/login',
      query: to.fullPath !== '/' ? { redirect: to.fullPath } : {},
    }
  }

  return true
})

// 全局后置钩子：设置浏览器标签页标题
router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - ${APP_TITLE}` : APP_TITLE
})

export default router
