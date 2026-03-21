import request from '@/utils/request'

// 创作中心 - 数据中心统计接口

// 核心概览 + 趋势 + 稿件表现
export const fetchCreatorOverview = (params) => {
  return request.get('/api/creator-stats/overview', { params })
}

