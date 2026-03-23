import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 0,
})

instance.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

function showError(error: unknown) {
  const err = error as { response?: { data?: { message?: string } }; message?: string }
  const message =
    err.response?.data?.message || err.message || '请求失败，请稍后重试'
  ElMessage.closeAll()
  ElMessage.error(message)
}

instance.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status as number | undefined
    if (status === 401) {
      const auth = useAuthStore()
      auth.clear()
      const path = window.location.pathname + window.location.search
      if (!path.startsWith('/login')) {
        window.location.replace(`/login?redirect=${encodeURIComponent(path)}`)
      }
      showError(error)
      return Promise.reject(error)
    }
    if (status === 403) {
      const silent = Boolean(error.config?.silent403)
      if (!silent) showError(error)
      return Promise.reject(error)
    }
    showError(error)
    return Promise.reject(error)
  },
)

export default instance
