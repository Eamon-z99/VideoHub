import request from '@/utils/request'

// 获取当前登录用户的资料（头像、签名等）
export const fetchMyProfile = () => {
  return request.get('/api/user/profile/me')
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


