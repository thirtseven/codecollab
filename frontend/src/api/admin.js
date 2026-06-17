import request from '@/utils/request'

export function getUserPage(params) {
  return request({ url: '/admin/users', method: 'get', params })
}

export function updateUserStatus(id, status) {
  return request({ url: `/admin/users/${id}/status?status=${status}`, method: 'put' })
}

export function updateUserRole(id, role) {
  return request({ url: `/admin/users/${id}/role?role=${role}`, method: 'put' })
}

export function deleteUser(id) {
  return request({ url: `/admin/users/${id}`, method: 'delete' })
}

export function getDashboardStats() {
  return request({ url: '/admin/dashboard', method: 'get', showLoading: false })
}

export function getSystemConfigs() {
  return request({ url: '/admin/configs', method: 'get' })
}

export function updateSystemConfig(key, value) {
  return request({ url: `/admin/configs/${key}?value=${encodeURIComponent(value)}`, method: 'put' })
}
