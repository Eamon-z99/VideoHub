import request from '@/utils/request'

// 普通视频列表（不带关键字，用于首页默认推荐）
export const fetchVideos = (
  page = 1,
  pageSize = 20,
  userId = null,
  followingOnly = false,
  followingId = null,
  tag = null,
  partitionTag = null,
  keyword = null,
  collectionId = undefined
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
  if (tag) {
    params.tag = tag
  }
  if (partitionTag) {
    params.partitionTag = partitionTag
  }
  if (keyword != null && String(keyword).trim() !== '') {
    params.keyword = String(keyword).trim()
  }
  if (collectionId !== undefined && collectionId !== null) {
    params.collectionId = collectionId
  }
  return request.get('/api/db/videos', { params })
}

/** UP 主投稿合集列表（含未分类稿件数 uncategorizedCount） */
export const listVideoCollections = (userId) => {
  return request.get('/api/db/video-collections', { params: { userId } })
}

export const createVideoCollection = (payload) => {
  return request.post('/api/db/video-collections', payload)
}

export const updateVideoCollection = (collectionId, payload) => {
  return request.put(`/api/db/video-collections/${collectionId}`, payload)
}

export const deleteVideoCollection = (collectionId) => {
  return request.delete(`/api/db/video-collections/${collectionId}`)
}

/** 将已发布视频移入某合集；collectionId 为 null 表示移出合集 */
export const setVideoCollectionForVideo = (videoId, collectionId) => {
  return request.put(
    `/api/db/video-collections/videos/${encodeURIComponent(videoId)}/collection`,
    { collectionId: collectionId === undefined ? null : collectionId }
  )
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

/** 内容管理：已发布 + 草稿 + 审核中投稿（tab: all|draft|reviewing|approved|rejected） */
export const listCreatorWorks = (params = {}) => {
  return request.get('/api/db/creator-works', { params })
}

export const getVideoDraftDetail = (submissionId) => {
  return request.get(`/api/db/video-drafts/${encodeURIComponent(submissionId)}`)
}

export const deleteVideoDraft = (submissionId) => {
  return request.delete(`/api/db/video-drafts/${encodeURIComponent(submissionId)}`)
}

// 内容管理：隐藏/取消隐藏已发布视频（仅作者）
export const hideCreatorWorkVideo = (videoId) => {
  return request.put(`/api/db/creator-works/videos/${encodeURIComponent(videoId)}/hide`)
}

export const unhideCreatorWorkVideo = (videoId) => {
  return request.put(`/api/db/creator-works/videos/${encodeURIComponent(videoId)}/unhide`)
}

