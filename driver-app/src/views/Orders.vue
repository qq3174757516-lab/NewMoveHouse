<template>
  <div class="page">
    <div class="toolbar"><h1>我的订单</h1><el-button :icon="Refresh" @click="load">刷新</el-button></div>
    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane label="全部" name=""/>
      <el-tab-pane label="已接单" name="ACCEPTED"/>
      <el-tab-pane label="进行中" name="RUNNING"/>
      <el-tab-pane label="待支付" name="MOVED"/>
      <el-tab-pane label="已完成" name="COMPLETED"/>
    </el-tabs>
    <el-skeleton :loading="loading" animated :rows="5">
      <el-empty v-if="orders.length===0" description="暂无订单"/>
      <article v-for="row in orders" :key="row.id" class="order-card">
        <div class="toolbar" style="justify-content:space-between">
          <span class="route">{{ row.startAddress }} → {{ row.endAddress }}</span>
          <el-tag :type="row.status==='COMPLETED'?'success':'warning'" round>{{ orderStatusCn(row.status) }}</el-tag>
        </div>
        <p class="muted">{{ row.appointmentTime||'尽快上门' }} · ￥{{ row.finalAmount }}</p>
        <div class="action-bar">
          <el-button v-if="row.status==='ACCEPTED'" type="primary" @click="next(row,'ARRIVE')">确认到达</el-button>
          <el-button v-if="row.status==='ARRIVED_LOADING'" type="primary" @click="next(row,'START_MOVE')">开始搬运</el-button>
          <el-button v-if="row.status==='MOVING'" type="primary" @click="next(row,'FINISH_MOVE')">完成搬运</el-button>
          <el-button @click="$router.push(`/orders/${row.id}`)">详情</el-button>
        </div>
      </article>
    </el-skeleton>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { orderStatusCn } from '../utils/orderStatus'

const tab = ref('')
const orders = ref([])
const loading = ref(false)
const running = ['ARRIVED_LOADING', 'MOVING']

async function load() {
  loading.value = true
  try {
    if (tab.value === 'RUNNING') {
      const groups = await Promise.all(running.map(status => http.get('/driver/orders', { params: { status } })))
      orders.value = groups.flat()
    } else {
      orders.value = await http.get('/driver/orders', { params: { status: tab.value } })
    }
  } finally { loading.value = false }
}

async function reportLocation() {
  const fallback = { lng: 112.94 + Math.random() / 100, lat: 28.22 + Math.random() / 100 }
  if (navigator.geolocation) {
    return new Promise(resolve => {
      navigator.geolocation.getCurrentPosition(async p => {
        await http.post('/driver/location', { lng: p.coords.longitude, lat: p.coords.latitude })
        resolve(true)
      }, async () => {
        await http.post('/driver/location', fallback)
        resolve(false)
      }, { enableHighAccuracy: true, timeout: 4000 })
    })
  }
  await http.post('/driver/location', fallback)
  return false
}

async function next(row, action) {
  if (action === 'ARRIVE' || action === 'FINISH_MOVE') {
    await reportLocation()
  }
  await http.post(`/driver/orders/${row.id}/status`, { action })
  ElMessage.success('状态已更新')
  load()
}

onMounted(load)
</script>
