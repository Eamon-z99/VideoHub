import axios from 'axios'
import { useUserStore } from '@/stores/user'

const instance = axios.create({
  baseURL: 'http://localhost:8080'
})

// 请求拦截器：自动添加Token
instance.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

// 响应拦截器：处理401未授权
instance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.clear() // 清除本地存储的Token
      window.location.href = '/login' // 跳转到登录页
    }
    return Promise.reject(error)
  }
)

export default instance