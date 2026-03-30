import request from './request'

export interface AdminVideosResponseBody {
  success: boolean
  data?: {
    list?: any[]
    total?: number
    page?: number
    pageSize?: number
  }
  message?: string
}

export function fetchAdminVideos(params: { page: number; pageSize: number; keyword?: string }) {
  return request.get<AdminVideosResponseBody>('/api/admin/videos', { params })
}

