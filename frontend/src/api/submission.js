import request from '@/utils/request'

export function getSubmissionPage(params) {
  return request({ url: '/submissions', method: 'get', params, showLoading: false })
}

export function getSubmissionDetail(id) {
  return request({ url: `/submissions/${id}`, method: 'get', showLoading: false })
}

export function createSubmission(data) {
  return request({ url: '/submissions', method: 'post', data })
}

export function reviewSubmission(id, status, comment) {
  return request({ url: `/submissions/${id}/review?status=${status}${comment ? '&comment=' + comment : ''}`, method: 'post' })
}

export function deleteSubmission(id) {
  return request({ url: `/submissions/${id}`, method: 'delete' })
}

export function getSubmissionFiles(id) {
  return request({ url: `/submissions/${id}/files`, method: 'get', showLoading: false })
}

export function getProjectSubmissions(projectId, limit = 10) {
  return request({ url: `/submissions/project/${projectId}?limit=${limit}`, method: 'get', showLoading: false })
}
