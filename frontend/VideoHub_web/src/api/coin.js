import request from '@/utils/request'

// 投币
export const coinVideo = (videoId) => {
  return request.post('/api/video-coins/coin', { videoId })
}

// 取消投币
export const uncoinVideo = (videoId) => {
  return request.post('/api/video-coins/uncoin', { videoId })
}

// 检查是否已投币（并返回最新投币数）
export const checkCoin = (videoId) => {
  return request.get('/api/video-coins/check', { params: { videoId } })
}

// 获取视频投币数
export const getCoinCount = (videoId) => {
  return request.get('/api/video-coins/count', { params: { videoId } })
}

// 获取当前登录用户硬币余额
export const getCoinBalance = () => {
  return request.get('/api/video-coins/balance')
}

