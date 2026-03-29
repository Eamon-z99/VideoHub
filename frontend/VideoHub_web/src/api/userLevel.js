import request from '@/utils/request'

// 获取当前登录用户等级进度
export const getUserLevelProgress = () => {
  return request.get('/api/user/level/progress')
}

/** 公开：指定用户等级进度（个人主页徽章，与下拉 /progress 同一套计算） */
export const getPublicUserLevelProgress = (userId) => {
  return request.get(`/api/user/level/public/${encodeURIComponent(String(userId))}`)
}

