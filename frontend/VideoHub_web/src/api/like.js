import request from '@/utils/request'

// 点赞视频
export const likeVideo = (videoId) => {
  return request.post('/api/video-likes/like', { videoId })
}

// 取消点赞
export const unlikeVideo = (videoId) => {
  return request.post('/api/video-likes/unlike', { videoId })
}

// 检查是否已点赞（并返回最新点赞数）
export const checkLike = (videoId) => {
  return request.get('/api/video-likes/check', { params: { videoId } })
}

// 获取视频点赞数
export const getLikeCount = (videoId) => {
  return request.get('/api/video-likes/count', { params: { videoId } })
}

// 获取用户点赞的视频列表
export const getUserLikedVideos = (page = 1, pageSize = 20) => {
  return request.get('/api/video-likes/list', { params: { page, pageSize } })
}


