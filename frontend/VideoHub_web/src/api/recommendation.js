import request from '@/utils/request'

/**
 * 推荐系统API
 */

/**
 * 获取用户推荐视频
 * @param {number} userId - 用户ID
 * @param {number} limit - 推荐数量
 * @returns {Promise}
 */
export function getRecommendations(userId, limit = 10) {
  return request({
    url: `/api/recommendations/user/${userId}`,
    method: 'get',
    params: { limit }
  })
}

/**
 * 基于物品的推荐
 * @param {number} userId - 用户ID
 * @param {number} limit - 推荐数量
 * @returns {Promise}
 */
export function getItemBasedRecommendations(userId, limit = 10) {
  return request({
    url: `/api/recommendations/item/${userId}`,
    method: 'get',
    params: { limit }
  })
}

/**
 * 检测用户是否为攻击者
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function detectUser(userId) {
  return request({
    url: `/api/defense/detect/${userId}`,
    method: 'get'
  })
}

/**
 * 获取可疑用户列表
 * @returns {Promise}
 */
export function getSuspiciousUsers() {
  return request({
    url: '/api/defense/suspicious',
    method: 'get'
  })
}



