import request from '@/utils/request'

export function login(data) {
  return request({ url: '/auth/login', method: 'post', data, showLoading: false })
}

export function register(data) {
  return request({ url: '/auth/register', method: 'post', data })
}

export function sendCode(email, type) {
  return request({ url: `/auth/send-code?email=${email}&type=${type}`, method: 'post', showLoading: false })
}

export function getUserInfo() {
  return request({ url: '/auth/user-info', method: 'get', showLoading: false })
}

export function updateProfile(data) {
  return request({ url: '/auth/profile', method: 'put', data })
}

export function changePassword(oldPassword, newPassword) {
  return request({ url: `/auth/password?oldPassword=${oldPassword}&newPassword=${newPassword}`, method: 'put' })
}

export function resetPassword(email, code, newPassword) {
  return request({ url: `/auth/reset-password?email=${email}&code=${code}&newPassword=${newPassword}`, method: 'post' })
}
