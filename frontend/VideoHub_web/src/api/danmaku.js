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

// 获取弹幕列表：一次性获取某视频全部弹幕（供右侧“弹幕列表”使用）
export function getDanmakuList (videoId) {
  return request({
    url: `/api/danmaku/${videoId}/all`,
    method: 'get'
  })
}

// 按日期（自然日）获取某视频的弹幕列表，date 格式：YYYY-MM-DD
export function getDanmakuByDate (videoId, date) {
  return request({
    url: `/api/danmaku/${videoId}/date`,
    method: 'get',
    params: { date }
  })
}

