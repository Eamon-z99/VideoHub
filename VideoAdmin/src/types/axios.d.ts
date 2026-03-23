import 'axios'

declare module 'axios' {
  interface AxiosRequestConfig {
    /** 为 true 时 403 不弹出 ElMessage（如会话校验） */
    silent403?: boolean
  }
}
