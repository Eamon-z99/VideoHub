import request from '@/utils/request'

export const addWatchLater = (userId, videoId) => {
  return request.post('/api/watch-later/add', { userId, videoId })
}

export const removeWatchLater = (userId, videoId) => {
  return request.post('/api/watch-later/remove', { userId, videoId })
}

export const removeWatchLaterById = (userId, id) => {
  return request.delete(`/api/watch-later/${encodeURIComponent(id)}`, { params: { userId } })
}

export const listWatchLater = (userId, page = 1, pageSize = 50) => {
  return request.get('/api/watch-later/list', { params: { userId, page, pageSize } })
}

export const checkWatchLater = (userId, videoId) => {
  return request.get('/api/watch-later/check', { params: { userId, videoId } })
}
