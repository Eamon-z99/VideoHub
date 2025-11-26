import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 使用环境变量和代理前缀，开发时可通过 /api 走 Vite 代理
const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const instance = axios.create({
  baseURL: BASE_URL,
  timeout: 15000
})

// 请求拦截器：自动添加 Token
instance.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

// 统一错误提示
function showErrorMessage(error) {
  const message = error.response?.data?.message || error.message || '请求失败，请稍后重试'
  ElMessage.closeAll()
  ElMessage.error(message)
}

// 响应拦截器：处理 2xx 外错误与 401 未授权
instance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.clear()
      const current = window.location.pathname + window.location.search
      const redirect = encodeURIComponent(current)
      window.location.href = `/login?redirect=${redirect}`
      return Promise.reject(error)
    }
    showErrorMessage(error)
    return Promise.reject(error)
  }
)

export default instance