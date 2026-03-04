import request from '@/utils/request'

// 获取某一时间段的弹幕
export function fetchDanmaku (videoId, from, to) {
  return request({
    url: `/api/danmaku/${videoId}`,
    method: 'get',
    params: { from, to }
  })
}

// 发送弹幕
export function sendDanmaku (videoId, payload) {
  return request({
    url: '/api/danmaku',
    method: 'post',
    params: { videoId },
    data: payload
  })
}

// 获取弹幕总数
export function getDanmakuCount (videoId) {
  return request({
    url: `/api/danmaku/${videoId}/count`,
    method: 'get'
  })
}


