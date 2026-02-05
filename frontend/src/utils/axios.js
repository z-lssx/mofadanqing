import axios from 'axios'

// 创建axios实例
const instance = axios.create({
  baseURL: '/api',
  timeout: 60000, // 请求超时时间 60s
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // 允许携带凭证
})

// 请求拦截器
instance.interceptors.request.use(
  config => {
    // 从sessionStorage获取token
    const token = sessionStorage.getItem('token')
    // 如果有token，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 处理请求错误
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  response => {
    // 返回响应数据
    return response.data
  },
  error => {
    // 处理响应错误
    console.error('响应错误:', error)
    // 如果是401未授权，跳转到登录页
    if (error.response && error.response.status === 401) {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('userInfo')
      // 跳转到登录页
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default instance
