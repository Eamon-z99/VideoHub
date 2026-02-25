import request from '@/utils/request'

// 普通视频列表（不带关键字，用于首页默认推荐）
export const fetchVideos = (
  page = 1,
  pageSize = 20,
  userId = null,
  followingOnly = false,
  followingId = null
) => {
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

// 按关键字搜索视频（JSON 请求体）
export const searchVideos = (payload) => {
  return request.post('/api/db/videos/search', payload)
}

// 上传视频（投稿）
export const uploadVideo = (formData) => {
  return request.post('/api/db/videos/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

