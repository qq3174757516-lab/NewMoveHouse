import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
const http=axios.create({baseURL:import.meta.env.VITE_API_BASE_URL||'/api',timeout:15000})
http.interceptors.request.use(c=>{
  const a=useAuthStore()
  if(a.token)c.headers.Authorization=`Bearer ${a.token}`
  if(c.data instanceof FormData)delete c.headers['Content-Type']
  return c
})
http.interceptors.response.use(r=>{
  const body=r.data
  if(body&&typeof body==='object'&&('code' in body)){
    if(body.code!==0) throw new Error(body.message||'请求失败')
    return body.data
  }
  return body
},e=>{
  const status=e.response?.status
  const msg=e.response?.data?.message||e.message||'请求失败'
  if(status===401){
    const a=useAuthStore(); a.clear()
    if(location.pathname!=='/login') location.href='/login'
  }
  ElMessage.error(msg); throw e
})
export default http
