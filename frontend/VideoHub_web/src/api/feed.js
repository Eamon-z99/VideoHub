import request from '@/utils/request'

const API_BASE = '/api/feeds'

/**
 * 获取关注用户的动态列表
 */
export const fetchFeeds = (page = 1, pageSize = 20, userId = null, followingOnly = false, followingId = null) => {
  const params = {
    page,
    pageSize
  }
  if (userId) {
    params.userId = userId
  }
  if (followingOnly) {
    params.followingOnly = true
  }
  if (followingId) {
    params.followingId = followingId
  }

  return request.get(API_BASE, { params })
}

/**
 * 个人主页：某用户发布的动态（公开）
 */
export const fetchFeedsByAuthor = (authorId, page = 1, pageSize = 20) => {
  return request.get(API_BASE, {
    params: { authorId, page, pageSize }
  })
}

/** 单条动态（用于动态页定位） */
export const fetchFeedById = (feedId) => {
  return request.get(`${API_BASE}/${encodeURIComponent(feedId)}`)
}

/**
 * 创建动态
 */
export const createFeed = (title, content, images = []) => {
  return request.post(API_BASE, {
    title,
    content,
    images
  })
}

/**
 * 删除动态
 */
export const deleteFeed = (feedId) => {
  return request.delete(`${API_BASE}/${feedId}`)
}

/**
 * 点赞动态
 */
export const likeFeed = (feedId) => {
  return request.post(`${API_BASE}/${feedId}/like`)
}

/**
 * 取消点赞动态
 */
export const unlikeFeed = (feedId) => {
  return request.post(`${API_BASE}/${feedId}/unlike`)
}

/**
 * 上传图片（用于动态）
 */
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/images/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

