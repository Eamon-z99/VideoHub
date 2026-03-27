import request from '@/utils/request'

export const recordSearchKeyword = (payload) => {
  // payload: { keyword: string, weight?: number }
  return request.post('/api/db/search/keyword-event', payload)
}

export const fetchHotKeywords = (params = {}) => {
  // params: { limit?: number }
  return request.get('/api/db/search/hot-keywords', { params })
}

