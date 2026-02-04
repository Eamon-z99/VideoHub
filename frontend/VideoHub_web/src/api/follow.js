import request from '@/utils/request'

// 关注用户
export const followUser = (followingId) => {
  return request.post('/api/follows/follow', { followingId })
}

// 取消关注
export const unfollowUser = (followingId) => {
  return request.post('/api/follows/unfollow', { followingId })
}

// 检查是否已关注
export const checkFollow = (followingId) => {
  return request.get('/api/follows/check', { params: { followingId } })
}

// 获取关注列表（仅ID）
export const getFollowingList = () => {
  return request.get('/api/follows/following')
}

// 获取关注用户的详细信息列表
export const getFollowingUsers = () => {
  return request.get('/api/follows/following/users')
}

// 获取用户统计信息
export const getUserStats = (userId) => {
  return request.get('/api/follows/stats', { params: { userId } })
}

