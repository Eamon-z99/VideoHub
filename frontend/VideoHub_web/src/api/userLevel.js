import request from '@/utils/request'

// 获取当前登录用户等级进度
export const getUserLevelProgress = () => {
  return request.get('/api/user/level/progress')
}

