<template>
  <el-container class="shell" direction="vertical">
    <el-header v-if="auth.token" class="topbar">
      <div class="brand">搬家用户端</div>
      <div class="spacer" />
      <el-button @click="logout">退出登录</el-button>
    </el-header>
    <el-container>
      <el-aside v-if="auth.token" width="220px" class="side">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/"><el-icon><Van /></el-icon><span>预约搬家</span></el-menu-item>
          <el-menu-item index="/orders"><el-icon><Tickets /></el-icon><span>我的订单</span></el-menu-item>
          <el-menu-item index="/announcements"><el-icon><Bell /></el-icon><span>系统公告</span></el-menu-item>
          <el-menu-item index="/complaints"><el-icon><Warning /></el-icon><span>投诉售后</span></el-menu-item>
          <el-menu-item index="/profile"><el-icon><User /></el-icon><span>个人中心</span></el-menu-item>
        </el-menu>
      </el-aside>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { connectWs } from './utils/ws'
import http from './api/http'
import { Bell, Tickets, User, Van, Warning } from '@element-plus/icons-vue'
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
let ws
function reconnect(token) { ws?.close(); ws = connectWs(token) }
onMounted(() => {
  reconnect(auth.token)
  if (!auth.token && route.path !== '/login') router.replace('/login')
})
watch(() => auth.token, token => reconnect(token))
onUnmounted(() => ws?.close())
async function logout() {
  try { await http.post('/auth/logout') } catch (_) {}
  auth.clear()
  router.push('/login')
}
</script>
<style scoped>
.topbar{display:flex;align-items:center;gap:12px;border-bottom:1px solid rgba(15,23,42,.08)}
.brand{font-weight:800}
.spacer{flex:1}
</style>
