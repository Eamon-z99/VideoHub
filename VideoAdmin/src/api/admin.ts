import request from './request'

export interface AdminMeBody {
  success: boolean
  message?: string
  adminId?: number
  displayName?: string
  loginAccount?: string
  /** 与 displayName 一致，便于兼容 */
  username?: string
  isAdmin?: boolean
}

export function fetchAdminMe() {
  return request.get<AdminMeBody>('/api/admin/me', { silent403: true })
}
