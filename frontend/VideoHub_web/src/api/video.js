import request from '@/utils/request'

export const fetchVideos = () => {
  return request.get('/api/db/videos')
}

export const fetchVideoDetail = (id) => {
  return request.get(`/api/db/videos/${encodeURIComponent(id)}`)
}


