import request from './request'

export interface AdminVideosByIdsResponseBody {
  success: boolean
  data?: {
    list?: any[]
  }
  message?: string
}

export function fetchAdminVideosByIds(videoIds: string[]) {
  return request.post<AdminVideosByIdsResponseBody>('/api/admin/videos/by-ids', {
    videoIds: videoIds || [],
  })
}

