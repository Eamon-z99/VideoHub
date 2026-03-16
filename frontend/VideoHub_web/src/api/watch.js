import request from '@/utils/request'

export function heartbeatWatch(videoId, clientId) {
  return request.post('/api/watch/heartbeat', { videoId, clientId })
}

export function getWatchCount(videoId) {
  return request.get('/api/watch/count', { params: { videoId } })
}


