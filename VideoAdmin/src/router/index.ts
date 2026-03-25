import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      component: () => import('@/views/AdminLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/pending' },
        {
          path: 'pending',
          name: 'pending',
          component: () => import('@/views/PendingReviewView.vue'),
          meta: { title: '待审核' },
        },
        {
          path: 'publish',
          name: 'publish',
          component: () => import('@/views/ApprovedUnpublishedView.vue'),
          meta: { title: '待发布' },
        },
        {
          path: 'complaints',
          name: 'complaints',
          component: () => import('@/views/VideoComplaintsView.vue'),
          meta: { title: '举报处理' },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.public) {
    if (auth.loggedIn && auth.isAdmin && to.path === '/login') {
      return (to.query.redirect as string) || '/'
    }
    return true
  }
  if (!auth.loggedIn || !auth.isAdmin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
