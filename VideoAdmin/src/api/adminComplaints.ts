import request from './request'

export interface VideoComplaintItem {
  id?: number
  videoId?: string
  videoTitle?: string
  reporterUserId?: number
  reporterUsername?: string
  category?: string
  otherDetail?: string
  evidenceUrls?: string | string[] | null
  status?: number
  handlerAdminId?: number | null
  handlerAction?: string | null
  handlerNote?: string | null
  createdTime?: string | null
}

export interface PagedComplaints {
  list: VideoComplaintItem[]
  page: number
  pageSize: number
  total: number
}

export function fetchPendingVideoComplaints(params: { page: number; pageSize: number }) {
  return request.get<{ success: boolean; message?: string; data?: PagedComplaints }>(
    '/api/admin/video-complaints/pending',
    { params },
  )
}

export function processVideoComplaint(
  complaintId: number | string,
  payload: { action: string; handlerNote?: string | null },
) {
  return request.post<{ success: boolean; message?: string }>(
    `/api/admin/video-complaints/${encodeURIComponent(String(complaintId))}/process`,
    payload,
  )
}

