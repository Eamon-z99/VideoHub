import request from '@/utils/request'

/**
 * 添加收藏
 */
export const addFavorite = (userId, videoId) => {
  return request.post('/api/favorites/add', { userId, videoId })
}

/**
 * 添加收藏（支持指定收藏夹）
 */
export const addFavoriteToFolder = (userId, videoId, folderId) => {
  return request.post('/api/favorites/add', { userId, videoId, folderId })
}

/**
 * 取消收藏
 */
export const removeFavorite = (userId, videoId) => {
  return request.post('/api/favorites/remove', { userId, videoId })
}

/**
 * 检查是否已收藏
 */
export const checkFavorite = (userId, videoId) => {
  return request.get('/api/favorites/check', { params: { userId, videoId } })
}

/**
 * 获取用户收藏列表
 */
export const getFavoriteList = (userId, page = 1, pageSize = 20) => {
  return request.get('/api/favorites/list', { params: { userId, page, pageSize } })
}

/**
 * 获取某个收藏夹下的收藏列表
 */
export const getFavoriteListByFolder = (userId, folderId, page = 1, pageSize = 20) => {
  return request.get('/api/favorites/list', { params: { userId, folderId, page, pageSize } })
}

/**
 * 删除单条收藏记录
 */
export const deleteFavorite = (favoriteId, userId) => {
  return request.delete(`/api/favorites/${favoriteId}`, { params: { userId } })
}

/**
 * 批量删除收藏记录
 */
export const batchDeleteFavorites = (userId, favoriteIds) => {
  return request({
    url: '/api/favorites/batch-delete',
    method: 'post',
    params: { userId },
    data: { ids: favoriteIds }
  })
}

