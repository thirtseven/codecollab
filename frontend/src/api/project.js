import request from '@/utils/request'

export function getProjectPage(params) {
  return request({ url: '/projects', method: 'get', params, showLoading: false })
}

export function getProjectDetail(id) {
  return request({ url: `/projects/${id}`, method: 'get', showLoading: false })
}

export function createProject(data) {
  return request({ url: '/projects', method: 'post', data })
}

export function updateProject(id, data) {
  return request({ url: `/projects/${id}`, method: 'put', data })
}

export function deleteProject(id) {
  return request({ url: `/projects/${id}`, method: 'delete' })
}

export function getProjectFiles(id) {
  return request({ url: `/projects/${id}/files`, method: 'get', showLoading: false })
}

export function getFileContent(fileId) {
  return request({ url: `/projects/files/${fileId}`, method: 'get', showLoading: false })
}

export function saveFile(projectId, data) {
  return request({ url: `/projects/${projectId}/files`, method: 'post', data, showLoading: false })
}

export function deleteFile(fileId) {
  return request({ url: `/projects/files/${fileId}`, method: 'delete' })
}

export function createDirectory(projectId, dirPath) {
  return request({ url: `/projects/${projectId}/directories?dirPath=${encodeURIComponent(dirPath)}`, method: 'post' })
}
