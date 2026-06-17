import request from '@/utils/request'

export function getTeamPage(params) {
  return request({ url: '/teams', method: 'get', params, showLoading: false })
}

export function getTeamDetail(id) {
  return request({ url: `/teams/${id}`, method: 'get', showLoading: false })
}

export function createTeam(data) {
  return request({ url: '/teams', method: 'post', data })
}

export function updateTeam(id, data) {
  return request({ url: `/teams/${id}`, method: 'put', data })
}

export function deleteTeam(id) {
  return request({ url: `/teams/${id}`, method: 'delete' })
}

export function getMyTeams() {
  return request({ url: '/teams/my', method: 'get', showLoading: false })
}

export function joinTeam(inviteCode) {
  return request({ url: `/teams/join?inviteCode=${inviteCode}`, method: 'post' })
}

export function leaveTeam(id) {
  return request({ url: `/teams/${id}/leave`, method: 'post' })
}

export function removeMember(teamId, userId) {
  return request({ url: `/teams/${teamId}/members/${userId}`, method: 'delete' })
}

export function updateMemberRole(teamId, userId, role) {
  return request({ url: `/teams/${teamId}/members/${userId}/role?role=${role}`, method: 'put' })
}

export function getTeamMembers(teamId) {
  return request({ url: `/teams/${teamId}/members`, method: 'get', showLoading: false })
}
