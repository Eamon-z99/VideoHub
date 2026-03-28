import request from '@/utils/request'

// 获取当前登录用户的资料（头像、签名等）
export const fetchMyProfile = () => {
  return request.get('/api/user/profile/me')
}

// 获取任意用户的公开资料（访客可访问，用于他人主页）
export const fetchPublicProfile = (userId) => {
  return request.get(`/api/user/profile/public/${encodeURIComponent(String(userId))}`)
}

// 更新个性签名（bio）
export const updateBio = (bio) => {
  return request.post('/api/user/profile/bio', { bio })
}

// 上传并更新头像
export const updateAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/user/profile/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 记录访问个人主页行为（用于“空间访客”统计）
export const recordProfileVisit = (profileUserId) => {
  return request.post('/api/user/profile/visit', { profileUserId })
}


