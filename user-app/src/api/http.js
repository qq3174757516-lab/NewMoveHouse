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
  if (res.data.code !== 0) throw new Error(res.data.message)
  return res.data.data
}, err => {
  ElMessage.error(err.response?.data?.message || err.message || '请求失败')
  throw err
})
export default http
