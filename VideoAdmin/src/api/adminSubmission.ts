import request from './request'

export interface PagedSubmissions {
  list: Record<string, unknown>[]
  page: number
  pageSize: number
  total: number
}

export interface SubmissionListResponse {
  success: boolean
  message?: string
  data?: PagedSubmissions
}

export function fetchPendingSubmissions(params: { page: number; pageSize: number }) {
  return request.get<SubmissionListResponse>('/api/admin/video-submissions/pending', { params })
}

export function fetchApprovedUnpublished(params: { page: number; pageSize: number }) {
  return request.get<SubmissionListResponse>('/api/admin/video-submissions/approved-unpublished', {
    params,
  })
}

export function approveSubmission(submissionId: string, comment: string) {
  return request.post<{ success: boolean; message?: string }>(
    `/api/admin/video-submissions/${encodeURIComponent(submissionId)}/approve`,
    { comment: comment || '' },
  )
}

export function rejectSubmission(submissionId: string, comment: string) {
  return request.post<{ success: boolean; message?: string }>(
    `/api/admin/video-submissions/${encodeURIComponent(submissionId)}/reject`,
    { comment: comment || '' },
  )
}

export function publishSubmission(submissionId: string, force: boolean) {
  return request.post<{ success: boolean; message?: string; videoId?: string }>(
    `/api/admin/video-submissions/${encodeURIComponent(submissionId)}/publish`,
    null,
    { params: { force: force ? 1 : 0 } },
  )
}

export function publishDue(limit = 50) {
  return request.post<{
    success: boolean
    message?: string
    data?: { processed?: number; published?: number }
  }>('/api/admin/video-submissions/publish-due', null, { params: { limit } })
}

export function fetchSubmissionPlayUrl(submissionId: string) {
  return request.get<{
    success: boolean
    message?: string
    data?: {
      submissionId?: string
      title?: string
      sourceFile?: string
      storagePath?: string
      playUrl?: string
    }
  }>(`/api/admin/video-submissions/${encodeURIComponent(submissionId)}/play-url`)
}

export function fetchPublishedCheck(submissionId: string) {
  return request.get<{
    success: boolean
    message?: string
    data?: {
      submissionId?: string
      publishedVideoId?: string
      videoFound?: boolean
      fileExists?: boolean
      status?: string
      playUrl?: string
    }
  }>(`/api/admin/video-submissions/${encodeURIComponent(submissionId)}/published-check`)
}
