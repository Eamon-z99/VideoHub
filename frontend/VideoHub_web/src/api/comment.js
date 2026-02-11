import request from '@/utils/request'

// 获取视频评论列表（顶层评论）
export const getComments = (videoId, page = 1, pageSize = 20, sort = 'time') => {
  return request.get('/api/comments', {
    params: { videoId, page, pageSize, sort }
  })
}

// 发表评论（顶层评论或回复）
export const addComment = (videoId, content, parentId = null) => {
  const payload = { videoId, content }
  if (parentId) {
    payload.parentId = parentId
  }
  return request.post('/api/comments/add', payload)
}

// 点赞评论
export const likeComment = (commentId) => {
  return request.post('/api/comments/like', { commentId })
}

// 取消点赞评论
export const unlikeComment = (commentId) => {
  return request.post('/api/comments/unlike', { commentId })
}

// 获取某条评论下的回复列表
export const getCommentReplies = (videoId, parentId) => {
  return request.get('/api/comments/replies', {
    params: { videoId, parentId }
  })
}


