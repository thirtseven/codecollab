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

export function resetUserPassword(id, newPassword) {
  return request({ url: `/admin/users/${id}/password?newPassword=${encodeURIComponent(newPassword)}`, method: 'put' })
}

export function updateUser(id, data) {
  return request({ url: `/admin/users/${id}/update?username=${encodeURIComponent(data.username || '')}&email=${encodeURIComponent(data.email || '')}&nickname=${encodeURIComponent(data.nickname || '')}&phone=${encodeURIComponent(data.phone || '')}&role=${data.role || ''}`, method: 'put' })
}

export function addUser(data) {
  return request({ url: '/auth/admin-register', method: 'post', data })
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
