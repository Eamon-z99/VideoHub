import request from '@/utils/request'

export const fetchVideos = (page = 1, pageSize = 20) => {
  return request.get('/api/db/videos', { params: { page, pageSize } })
}

export const fetchVideoDetail = (id) => {
  return request.get(`/api/db/videos/${encodeURIComponent(id)}`)
}

