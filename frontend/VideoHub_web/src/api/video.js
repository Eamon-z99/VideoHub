import request from '@/utils/request'

export const fetchVideos = (page = 1, pageSize = 20, userId = null, followingOnly = false, followingId = null) => {
  const params = { page, pageSize }
  if (userId) {
    params.userId = userId
  }
  if (followingOnly) {
    params.followingOnly = true
  }
  if (followingId) {
    params.followingId = followingId
  }
  return request.get('/api/db/videos', { params })
}

export const fetchVideoDetail = (id) => {
  return request.get(`/api/db/videos/${encodeURIComponent(id)}`)
}

export const fetchTopVideos = (limit = 6) => {
  return request.get('/api/db/videos/top', { params: { limit } })
}

// 获取指定UP的其它视频
export const fetchVideosByUploader = (uploaderId, limit = 4, excludeVideoId = null) => {
  const params = { uploaderId, limit }
  if (excludeVideoId) {
    params.excludeVideoId = excludeVideoId
  }
  return request.get('/api/db/videos/by-uploader', { params })
}

