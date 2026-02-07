import request from '@/utils/request'

// 获取关注好友会话列表
export function fetchContacts() {
  return request({
    url: '/api/messages/contacts',
    method: 'get'
  })
}

// 获取与某个好友的聊天记录
export function fetchMessageHistory(peerId, page = 1, pageSize = 50) {
  return request({
    url: '/api/messages/history',
    method: 'get',
    params: { peerId, page, pageSize }
  })
}

// 发送私信
export function sendPrivateMessage(peerId, content) {
  return request({
    url: '/api/messages/send',
    method: 'post',
    data: { peerId, content }
  })
}


