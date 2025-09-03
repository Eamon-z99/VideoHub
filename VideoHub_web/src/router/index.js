import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/views/HomeView/home.vue'
import VideoStart from '@/views/VideoStart/VideoStart.vue'
const History = () => import('@/views/History/History.vue')
const UserProfile = () => import('@/views/UserProfile/UserProfile.vue')
const FeedHome = () => import('@/views/FeedHome/FeedHome.vue')

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomePage
  },
  {
    path: '/video/:id',
    name: 'video',
    component: VideoStart
  },
  {
    path: '/profile/:id?',
    name: 'profile',
    component: UserProfile
  },
  {
    path: '/feed',
    name: 'feed',
    component: FeedHome
  },
  {
    path: '/history',
    name: 'history',
    component: History
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
