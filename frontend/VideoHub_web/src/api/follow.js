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

// 获取我的粉丝列表（包含是否回关）
export const getFansUsers = () => {
  return request.get('/api/follows/fans/users')
}

// 移除粉丝
export const removeFan = (followerId) => {
  return request.post('/api/follows/fans/remove', { followerId })
}

// 获取用户统计信息
export const getUserStats = (userId) => {
  return request.get('/api/follows/stats', { params: { userId } })
}

/** 个人主页：某用户的关注列表（公开；登录时返回 iFollow）
 * @param {{ sort?: 'recent' | 'active' }} [params] sort：recent 最近关注；active 最常访问（按主页访问记录）
 */
export const getProfileFollowingUsers = (userId, params = {}) => {
  return request.get(`/api/follows/profile/${userId}/following`, { params })
}

/** 个人主页：某用户的粉丝列表
 * @param {{ sort?: 'recent' | 'active' }} [params] sort：recent 最近粉丝；active 最常访问（粉丝访问本主页次数）
 */
export const getProfileFansUsers = (userId, params = {}) => {
  return request.get(`/api/follows/profile/${userId}/fans`, { params })
}

/** 我的关注分组列表（需登录） */
export const listFollowGroups = () => {
  return request.get('/api/follows/groups')
}

/** 新建关注分组（需登录） */
export const createFollowGroup = (name) => {
  return request.post('/api/follows/groups', { name })
}

/** 删除关注分组（需登录） */
export const deleteFollowGroup = (groupId) => {
  return request.delete(`/api/follows/groups/${groupId}`)
}

/**
 * 将已关注用户移入/移出分组（需登录）
 * @param {number} followingId 被关注用户 id
 * @param {number|null|undefined} groupId 分组 id；null/undefined 表示未分组
 */
export const setFollowingGroup = (followingId, groupId) => {
  return request.put('/api/follows/following/group', {
    followingId,
    groupId: groupId ?? null
  })
}

