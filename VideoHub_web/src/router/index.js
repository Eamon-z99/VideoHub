import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/views/HomeView/home.vue'
import VideoStart from '@/views/VideoStart/VideoStart.vue'
import { useUserStore } from '@/stores/user'
const History = () => import('@/views/History/History.vue')
const UserProfile = () => import('@/views/UserProfile/UserProfile.vue')
const FeedHome = () => import('@/views/FeedHome/FeedHome.vue')
const Column = () => import('@/views/column/Column.vue')
const MusicChart = () => import('@/views/column/MusicChart.vue')
const SubmitHome = () => import('@/views/Submit/submit_home.vue')
const DataCenter = () => import('@/views/Submit/data_center.vue')
const ContentManagement = () => import('@/views/Submit/content_management.vue')

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
    component: History,
    meta: {requiresAuth: true}
  },
  {
    path: '/column',
    name: 'column',
    component: Column
  },
  {
    path: '/music-chart',
    name: 'music-chart',
    component: MusicChart
  },
  {
    path: '/submitHome',
    name: 'submitHome',
    component: SubmitHome
  },
  {
    path: '/dataCenter',
    name: 'dataCenter',
    component: DataCenter
  },
  {
    path: '/contentManagement',
    name: 'contentManagement',
    component: ContentManagement
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.token) {
    return { path: '/', query: { redirect: to.fullPath } }
  }
})
export default router
