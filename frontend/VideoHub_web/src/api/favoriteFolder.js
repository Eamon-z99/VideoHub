import request from '@/utils/request'

export const getFavoriteFolderList = (userId) => {
  return request({
    url: '/api/favorite-folders/list',
    method: 'get',
    params: { userId }
  })
}

export const createFavoriteFolder = (userId, name, isPublic = true) => {
  return request({
    url: '/api/favorite-folders/create',
    method: 'post',
    data: { userId, name, isPublic }
  })
}

export const renameFavoriteFolder = (userId, folderId, name) => {
  return request({
    url: '/api/favorite-folders/rename',
    method: 'post',
    data: { userId, folderId, name }
  })
}

export const deleteFavoriteFolder = (userId, folderId) => {
  return request({
    url: `/api/favorite-folders/${folderId}`,
    method: 'delete',
    params: { userId }
  })
}


