import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
const http=axios.create({baseURL:import.meta.env.VITE_API_BASE_URL||'/api',timeout:15000})
http.interceptors.request.use(c=>{const a=useAuthStore(); if(a.token)c.headers.Authorization=`Bearer ${a.token}`; return c})
http.interceptors.response.use(r=>{if(r.data.code!==0)throw new Error(r.data.message);return r.data.data},e=>{ElMessage.error(e.response?.data?.message||e.message||'请求失败');throw e})
export default http
