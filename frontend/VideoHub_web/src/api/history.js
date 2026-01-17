import request from '@/utils/request'

/**
 * 播放历史API
 */

/**
 * 记录播放历史
 * @param {number} userId - 用户ID
 * @param {string} videoId - 视频ID
 * @param {number} playTime - 播放到的时间点（秒）
 * @param {number} duration - 视频总时长（秒）
 * @returns {Promise}
 */
export function recordHistory(userId, videoId, playTime, duration) {
  return request({
    url: '/api/history/record',
    method: 'post',
    data: {
      userId,
      videoId,
      playTime,
      duration
    }
  })
}

/**
 * 获取用户播放历史列表
 * @param {number} userId - 用户ID
 * @param {number} page - 页码
 * @param {number} pageSize - 每页数量
 * @returns {Promise}
 */
export function getHistoryList(userId, page = 1, pageSize = 20) {
  return request({
    url: '/api/history/list',
    method: 'get',
    params: {
      userId,
      page,
      pageSize
    }
  })
}

/**
 * 删除单条历史记录
 * @param {number} historyId - 历史记录ID
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function deleteHistory(historyId, userId) {
  return request({
    url: `/api/history/${historyId}`,
    method: 'delete',
    params: { userId }
  })
}

/**
 * 清空所有历史记录
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function clearAllHistory(userId) {
  return request({
    url: '/api/history/clear',
    method: 'delete',
    params: { userId }
  })
}

/**
 * 批量删除历史记录
 * @param {number} userId - 用户ID
 * @param {Array<number>} ids - 历史记录ID数组
 * @returns {Promise}
 */
export function batchDeleteHistory(userId, ids) {
  return request({
    url: '/api/history/batch-delete',
    method: 'post',
    params: { userId },
    data: { ids }
  })
}

