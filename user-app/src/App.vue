<template>
  <el-container class="shell">
    <el-aside v-if="auth.token" width="220px" class="side">
      <h2>搬家用户端</h2>
      <el-menu router :default-active="$route.path">
        <el-menu-item index="/"><el-icon><Van /></el-icon><span>预约搬家</span></el-menu-item>
        <el-menu-item index="/orders"><el-icon><Tickets /></el-icon><span>我的订单</span></el-menu-item>
        <el-menu-item index="/profile"><el-icon><User /></el-icon><span>个人中心</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-main><router-view /></el-main>
  </el-container>
</template>
<script setup>
import { onMounted, onUnmounted, watch } from 'vue'
import { useAuthStore } from './stores/auth'
import { connectWs } from './utils/ws'
import { Tickets, User, Van } from '@element-plus/icons-vue'
const auth = useAuthStore()
let ws
function reconnect(token) { ws?.close(); ws = connectWs(token) }
onMounted(() => { reconnect(auth.token) })
watch(() => auth.token, token => reconnect(token))
onUnmounted(() => ws?.close())
</script>
