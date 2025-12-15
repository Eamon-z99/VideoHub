import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/videohub' // 默认进入子应用首页
  },
  {
    path: '/videohub/:pathMatch(.*)*',
    component: () => import('@/views/MicroAppContainer.vue')
  },
  // 其他路径统一回到子应用首页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/videohub'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router

