import request from './request'

/** 管理端独立登录 POST /api/admin/auth/login */
export interface AdminLoginResponseBody {
  success: boolean
  message?: string
  token?: string
  adminId?: number
  account?: string
  displayName?: string
  isAdmin?: boolean
}

export function adminLoginApi(account: string, password: string) {
  return request.post<AdminLoginResponseBody>('/api/admin/auth/login', { account, password })
}
