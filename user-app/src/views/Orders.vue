<template>
  <div class="page">
    <div class="toolbar"><h1>我的订单</h1><el-button :icon="Refresh" @click="load">刷新</el-button></div>
    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待接单" name="WAITING_ACCEPT" />
      <el-tab-pane label="进行中" name="RUNNING" />
      <el-tab-pane label="已完成" name="COMPLETED" />
      <el-tab-pane label="已取消" name="CANCELED" />
    </el-tabs>
    <el-skeleton :loading="loading" animated :rows="5">
      <el-empty v-if="orders.length===0" description="暂无订单，去发布一个搬家需求吧" />
      <div v-else>
        <article v-for="o in orders" :key="o.id" class="order-card" @click="$router.push(`/orders/${o.id}`)">
          <div class="toolbar" style="justify-content:space-between">
            <b>订单号 #{{ o.id }}</b>
            <StatusTag :status="o.status" />
          </div>
          <div class="route">{{ o.startAddress }} → {{ o.endAddress }}</div>
          <p class="muted">预约时间：{{ o.appointmentTime || '尽快上门' }} · {{ o.vehicleTypeName || '搬家车型' }}</p>
          <div class="toolbar" style="justify-content:space-between;margin:0">
            <span class="money">￥{{ o.finalAmount }}</span>
            <el-button link type="primary">查看详情</el-button>
          </div>
        </article>
      </div>
    </el-skeleton>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import http from '../api/http'
import StatusTag from '../components/StatusTag.vue'
const tab=ref(''); const orders=ref([]); const loading=ref(false)
const running = ['ACCEPTED','ARRIVED_LOADING','MOVING','MOVED']
async function load(){
  loading.value=true
  try{
    if(tab.value==='RUNNING'){
      const groups = await Promise.all(running.map(status => http.get('/user/orders',{params:{status}})))
      orders.value = groups.flat()
    } else {
      orders.value=await http.get('/user/orders',{params:{status:tab.value}})
    }
  } finally { loading.value=false }
}
onMounted(load)
</script>
