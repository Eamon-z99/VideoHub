import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/views/HomeView/home.vue'
import VideoStart from '@/views/VideoStart/VideoStart.vue'
import { useUserStore } from '@/stores/user'
const Vip = () => import('@/views/Vip/Vip.vue')
const LoginPage = () => import('@/views/Login/LoginPage.vue')
const History = () => import('@/views/History/History.vue')
const UserProfile = () => import('@/views/UserProfile/UserProfile.vue')
const FeedHome = () => import('@/views/FeedHome/FeedHome.vue')
const Column = () => import('@/views/column/Column.vue')
const MusicChart = () => import('@/views/column/MusicChart.vue')
const Messages = () => import('@/views/Messages/Messages.vue')
const MessageSettings = () => import('@/views/Messages/MessageSettings.vue')
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
    path: '/vip',
    name: 'vip',
    component: Vip
  },
  {
    path: '/login',
    name: 'login',
    component: LoginPage,
    meta: { guestOnly: true }
  },
  {
    path: '/video/:id',
    name: 'video',
    component: VideoStart
  },
  {
    path: '/profile/:id?',//收藏页
    name: 'profile',
    component: UserProfile
  },
  {
    path: '/feed',//动态页
    name: 'feed',
    component: FeedHome
  },
  {
    path: '/messages',//消息中心
    name: 'messages',
    component: Messages
  },
  {
    path: '/messages/settings',//消息中心——消息设置
    name: 'message-settings',
    component: MessageSettings
  },
  {
    path: '/history',//历史主页
    name: 'history',
    component: History,
    //meta: {requiresAuth: true} //路由守卫
  },
  {
    path: '/column',//专栏
    name: 'column',
    component: Column
  },
  {
    path: '/music-chart',//新歌热榜
    name: 'music-chart',
    component: MusicChart
  },
  {
    path: '/submitHome',//创作中心
    name: 'submitHome',
    component: SubmitHome
  },
  {
    path: '/dataCenter',//数据中心
    name: 'dataCenter',
    component: DataCenter
  },
  {
    path: '/contentManagement',//内容管理
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
  const isAuthed = !!userStore.token
  if (to.meta.requiresAuth && !isAuthed) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && isAuthed) {
    const redirect = to.query.redirect || '/'
    return typeof redirect === 'string' ? redirect : '/'
  }
})
export default router
