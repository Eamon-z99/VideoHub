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

// 创建投稿（上传视频文件到投稿流）
export const uploadVideo = (formData, params = {}) => {
  return request.post('/api/db/videos/upload', formData, {
    params,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// 二步投稿：上传完成后更新投稿信息（不重新上传视频）
export const updateVideoSubmission = (submissionId, formData, params = {}) => {
  return request.post(`/api/db/video-submissions/${encodeURIComponent(submissionId)}/update`, formData, {
    params,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export const listVideoDrafts = (params = {}) => {
  return request.get('/api/db/video-drafts', { params })
}

export const getVideoDraftDetail = (submissionId) => {
  return request.get(`/api/db/video-drafts/${encodeURIComponent(submissionId)}`)
}

export const deleteVideoDraft = (submissionId) => {
  return request.delete(`/api/db/video-drafts/${encodeURIComponent(submissionId)}`)
}

