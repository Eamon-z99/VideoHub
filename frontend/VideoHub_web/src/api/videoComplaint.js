import request from '@/utils/request'

// 用户提交稿件举报
export const createVideoComplaint = (videoId, category, detail, evidenceUrls = []) => {
  return request.post('/api/video-complaints', {
    videoId,
    category,
    // 后端兼容：detail 或 otherDetail
    detail,
    evidenceUrls
  })
}

