import request from '@/utils/request'

export function chat(data) {
  return request({ url: '/ai/chat', method: 'post', data, timeout: 60000 })
}

export function getConversations() {
  return request({ url: '/ai/conversations', method: 'get', showLoading: false })
}

export function getConversationMessages(id) {
  return request({ url: `/ai/conversations/${id}/messages`, method: 'get', showLoading: false })
}

export function deleteConversation(id) {
  return request({ url: `/ai/conversations/${id}`, method: 'delete' })
}
