import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/videohub'
  },
  {
    path: '/videohub',
    name: 'videohub',
    component: () => import('@/views/MicroAppContainer.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router

