import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const http = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || '/api', timeout: 15000 })
http.interceptors.request.use(config => {
  const auth = useAuthStore()
  if (auth.token) config.headers.Authorization = `Bearer ${auth.token}`
  return config
})
http.interceptors.response.use(res => {
  const body = res.data
  if (body && typeof body === 'object' && 'code' in body) {
    if (body.code !== 0) throw new Error(body.message || '请求失败')
    return body.data
  }
  return body
}, err => {
  const status = err.response?.status
  const msg = err.response?.data?.message || err.message || '请求失败'
  if (status === 401) {
    const auth = useAuthStore()
    auth.clear()
    if (location.pathname !== '/login') location.href = '/login'
  }
  ElMessage.error(msg)
  throw err
})
export default http
