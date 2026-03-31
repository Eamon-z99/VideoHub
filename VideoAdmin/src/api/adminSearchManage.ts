import request from './request'

export interface PagedResp<T = any> {
  list: T[]
  page: number
  pageSize: number
  total: number
}

export function fetchKeywordStats(params: { page: number; pageSize: number; keyword?: string }) {
  return request.get<{ success: boolean; message?: string; data?: PagedResp }>(
    '/api/admin/search/keyword-stats',
    { params },
  )
}

export function fetchKeywordEvents(params: { keyword: string; page: number; pageSize: number }) {
  return request.get<{ success: boolean; message?: string; data?: PagedResp }>(
    '/api/admin/search/keyword-events',
    { params },
  )
}

export function fetchUserEvents(params: { userId: number | string; page: number; pageSize: number }) {
  return request.get<{ success: boolean; message?: string; data?: PagedResp }>(
    '/api/admin/search/user-events',
    { params },
  )
}

export function fetchHotSlots() {
  return request.get<{ success: boolean; message?: string; data?: { list?: any[] } }>(
    '/api/admin/search/hot-slots',
  )
}

export function saveHotSlots(list: Array<{ slot: number; keyword: string; badge: '' | 'NEW' | 'HOT' }>) {
  return request.post<{ success: boolean; message?: string }>('/api/admin/search/hot-slots', { list })
}

