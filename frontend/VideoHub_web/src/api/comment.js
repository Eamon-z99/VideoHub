import request from '@/utils/request'

// 获取视频评论列表（顶层评论）
export const getComments = (videoId, page = 1, pageSize = 20, sort = 'time') => {
  return request.get('/api/comments', {
    params: { videoId, page, pageSize, sort }
  })
}

// 发表评论（顶层评论或回复）；replyToCommentId 为对某条「回复」再回复时传入该回复的评论 id
export const addComment = (videoId, content, parentId = null, replyToCommentId = null) => {
  const payload = { videoId, content }
  if (parentId) {
    payload.parentId = parentId
  }
  if (replyToCommentId != null && replyToCommentId !== '') {
    payload.replyToCommentId = replyToCommentId
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

// 获取视频评论总数（包含回复）
export const getCommentCountWithReplies = (videoId) => {
  return request.get('/api/comments/count-with-replies', {
    params: { videoId }
  })
}

/** 评论配图上传（复用动态图片存储，返回路径如 /feed-images/...） */
export const uploadCommentImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/images/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}


