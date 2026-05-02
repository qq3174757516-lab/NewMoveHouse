<template><el-container class="shell"><el-aside v-if="auth.token" width="220px" class="side"><h2>搬家司机端</h2><el-menu router :default-active="$route.path"><el-menu-item index="/">接单大厅</el-menu-item><el-menu-item index="/orders">我的订单</el-menu-item><el-menu-item index="/income">收入统计</el-menu-item><el-menu-item index="/profile">个人资料</el-menu-item></el-menu></el-aside><el-main><router-view /></el-main></el-container></template>
<script setup>
import { onMounted,onUnmounted,watch } from 'vue'
import { useAuthStore } from './stores/auth'
import { connectWs } from './utils/ws'
const auth=useAuthStore(); let ws
function reconnect(token){ws?.close();ws=connectWs(token)}
onMounted(()=>reconnect(auth.token))
watch(()=>auth.token,token=>reconnect(token))
onUnmounted(()=>ws?.close())
</script>
