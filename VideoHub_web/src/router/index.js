import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView/HomeView.vue'
import VideoStart from '@/views/VideoStart/VideoStart.vue'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/video/:id',
      name: 'video-start',
      component: VideoStart,
    }
  ]
})

export default router