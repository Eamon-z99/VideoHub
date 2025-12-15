import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/views/HomeView/home.vue'
import VideoStart from '@/views/VideoStart/VideoStart.vue'
import { useUserStore } from '@/stores/user'
const Vip = () => import('@/views/Vip/Vip.vue')
const MemberMall = () => import('@/views/Mall/MemberMall.vue')
const Login = () => import('@/components/Login.vue')
const History = () => import('@/views/History/History.vue')
const UserProfile = () => import('@/views/UserProfile/UserProfile.vue')
const FeedHome = () => import('@/views/FeedHome/FeedHome.vue')
const Column = () => import('@/views/column/Column.vue')
const MusicChart = () => import('@/views/column/MusicChart.vue')
const Live = () => import('@/views/column/Live.vue')
const Messages = () => import('@/views/Messages/Messages.vue')
const MessageSettings = () => import('@/views/Messages/MessageSettings.vue')
const SubmitHome = () => import('@/views/Submit/submit_home.vue')
const DataCenter = () => import('@/views/Submit/data_center.vue')
const ContentManagement = () => import('@/views/Submit/content_management.vue')
const Community = () => import('@/views/column/Community.vue')
const Classroom = () => import('@/views/column/Classroom.vue')

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomePage
  },
  {
    path: '/mall',//会员购
    name: 'mall',
    component: MemberMall
  },
  {
    path: '/live',
    name: 'live',
    component: Live
  },
  {
    path: '/vip',
    name: 'vip',
    component: Vip
  },
  {
    path: '/login',
    name: 'login',
    component: Login,
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
    path: '/community',//社区中心
    name: 'community',
    component: Community
  },
  {
    path: '/classroom',//课堂
    name: 'classroom',
    component: Classroom
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

// 获取路由 base，支持作为子应用运行
const getRouterBase = () => {
  // 如果是 qiankun 环境，使用 __MICRO_APP_BASE_ROUTE__ 或默认 /videohub
  if (window.__POWERED_BY_QIANKUN__) {
    return window.__MICRO_APP_BASE_ROUTE__ || '/videohub';
  }
  return import.meta.env.BASE_URL;
};

const router = createRouter({
  history: createWebHistory(getRouterBase()),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthed = !!userStore.token
  
  // 如果访问登录页且已登录，重定向到首页或redirect参数指定的页面
  if (to.meta.guestOnly && isAuthed) {
    // 避免循环重定向：如果redirect参数包含/login，直接跳转到首页
    const redirect = to.query.redirect
    if (redirect && typeof redirect === 'string') {
      // 解码redirect参数
      try {
        const decodedRedirect = decodeURIComponent(redirect)
        // 如果redirect不是登录页，且不包含/login，才跳转
        if (decodedRedirect !== '/login' && !decodedRedirect.includes('/login')) {
          next(decodedRedirect)
          return
        }
      } catch (e) {
        console.error('解码redirect参数失败:', e)
      }
    }
    // 默认跳转到首页
    next('/')
    return
  }
  
  // 如果需要认证但未登录，重定向到登录页
  if (to.meta.requiresAuth && !isAuthed) {
    // 避免循环：如果当前已经在登录页，不要添加redirect参数
    if (to.path !== '/login') {
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      next()
    }
    return
  }
  
  // 其他情况正常通过
  next()
})
export default router
