<template>
  <el-container class="shell" direction="vertical">
    <el-header v-if="auth.token" class="topbar">
      <div class="brand">搬家管理后台</div>
      <div class="spacer" />
      <el-button @click="logout">退出登录</el-button>
    </el-header>
    <el-container>
      <el-aside v-if="auth.token" width="220px" class="side">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/">数据看板</el-menu-item>
          <el-menu-item index="/users">用户管理</el-menu-item>
          <el-menu-item index="/drivers">司机审核</el-menu-item>
          <el-menu-item index="/orders">订单管理</el-menu-item>
          <el-menu-item index="/admin/vehicle-types">车辆类型管理</el-menu-item>
          <el-menu-item index="/announcements">系统公告</el-menu-item>
          <el-menu-item index="/complaints">投诉处理</el-menu-item>
          <el-menu-item index="/pricing">计价规则</el-menu-item>
          <el-menu-item index="/reviews">评价管理</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import http from './api/http'
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
onMounted(() => {
  if (!auth.token && route.path !== '/login') router.replace('/login')
})
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
