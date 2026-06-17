import axios from 'axios'
import { Message, Loading } from 'element-ui'
import store from '@/store'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

let loadingInstance = null

service.interceptors.request.use(
  config => {
    const token = store.state.user.token
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    if (config.showLoading !== false) {
      loadingInstance = Loading.service({ fullscreen: true, background: 'rgba(0, 0, 0, 0.1)' })
    }
    return config
  },
  error => {
    if (loadingInstance) loadingInstance.close()
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    if (loadingInstance) loadingInstance.close()
    const res = response.data
    if (res.code !== 200) {
      Message({ message: res.message || '请求失败', type: 'error', duration: 3000 })
      if (res.code === 401) {
        store.dispatch('user/logout')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (loadingInstance) loadingInstance.close()
    if (error.response) {
      switch (error.response.status) {
        case 401:
          store.dispatch('user/logout')
          router.push('/login')
          break
        case 403:
          Message({ message: '无权限访问', type: 'error' })
          break
        case 404:
          Message({ message: '请求的资源不存在', type: 'error' })
          break
        case 500:
          Message({ message: '服务器内部错误', type: 'error' })
          break
        case 502:
          Message({ message: '网关错误', type: 'error' })
          break
        case 503:
          Message({ message: '服务不可用', type: 'error' })
          break
        default:
          Message({ message: `请求失败 (${error.response.status})`, type: 'error' })
      }
    } else if (error.message.includes('timeout')) {
      Message({ message: '请求超时，请稍后重试', type: 'error' })
    } else if (error.message.includes('Network Error')) {
      Message({ message: '网络连接失败，请检查网络', type: 'error' })
    }
    return Promise.reject(error)
  }
)

export default service
