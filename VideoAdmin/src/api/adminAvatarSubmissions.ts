import request from './request'

export interface PagedAvatarSubmissions {
  list: Record<string, unknown>[]
  page: number
  pageSize: number
  total: number
}

export interface AvatarSubmissionListResponse {
  success: boolean
  message?: string
  data?: PagedAvatarSubmissions
}

export function fetchPendingAvatarSubmissions(params: { page: number; pageSize: number }) {
  return request.get<AvatarSubmissionListResponse>('/api/admin/avatar-submissions/pending', { params })
}

export function approveAvatarSubmission(id: number | string, comment: string) {
  return request.post<{ success: boolean; message?: string }>(
    `/api/admin/avatar-submissions/${encodeURIComponent(String(id))}/approve`,
    { comment: comment || '' },
  )
}

export function rejectAvatarSubmission(id: number | string, comment: string) {
  return request.post<{ success: boolean; message?: string }>(
    `/api/admin/avatar-submissions/${encodeURIComponent(String(id))}/reject`,
    { comment: comment || '' },
  )
}

