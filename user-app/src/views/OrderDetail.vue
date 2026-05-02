<template>
  <div class="page">
    <div class="toolbar"><h1>订单 #{{ order?.id }}</h1><el-button :icon="Refresh" @click="load">刷新</el-button></div>
    <el-skeleton :loading="loading" animated :rows="8">
      <div v-if="order" class="grid">
        <div>
          <div class="card">
            <div class="toolbar" style="justify-content:space-between">
              <h2>订单进度</h2><StatusTag :status="order.status" />
            </div>
            <el-timeline>
              <el-timeline-item v-for="l in order.logs" :key="l.id" :timestamp="l.createdAt" placement="top">
                {{ l.fromStatus || '创建' }} → {{ l.toStatus }} {{ l.remark }}
              </el-timeline-item>
            </el-timeline>
          </div>
          <div class="card">
            <h2>司机信息</h2>
            <div v-if="order.driverId" class="driver-card">
              <div class="avatar">司</div>
              <div><b>{{ order.driverName }}</b><p class="muted">{{ order.vehiclePlate }} · {{ order.driverPhone }}</p><el-rate :model-value="5" disabled size="small"/></div>
            </div>
            <el-empty v-else description="等待司机接单" />
          </div>
        </div>
        <div>
          <div class="card">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="路线">{{ order.startAddress }} → {{ order.endAddress }}</el-descriptions-item>
              <el-descriptions-item label="车型">{{ order.vehicleTypeName }}</el-descriptions-item>
              <el-descriptions-item label="金额">￥{{ order.finalAmount }}</el-descriptions-item>
              <el-descriptions-item label="支付">{{ order.paymentStatus }}</el-descriptions-item>
            </el-descriptions>
            <div class="action-bar">
              <el-button v-if="order.status==='MOVED'" type="primary" :icon="Wallet" @click="$router.push(`/payment/${order.id}`)">去支付</el-button>
              <el-button v-if="order.status==='WAITING_ACCEPT'" @click="cancel">取消订单</el-button>
            </div>
          </div>
          <div class="card">
            <h2>司机实时位置</h2>
            <DriverMap :location="driverLocation" />
          </div>
          <div v-if="order.status==='COMPLETED'" class="card">
            <h2>评价司机</h2>
            <el-form :model="review" label-width="70px">
              <el-form-item label="评分"><el-rate v-model="review.rating"/></el-form-item>
              <el-form-item label="评价"><el-input v-model="review.content" type="textarea"/></el-form-item>
              <el-button type="primary" :loading="reviewing" @click="submitReview">提交评价</el-button>
            </el-form>
          </div>
        </div>
      </div>
    </el-skeleton>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, Wallet } from '@element-plus/icons-vue'
import http from '../api/http'
import DriverMap from '../components/DriverMap.vue'
import StatusTag from '../components/StatusTag.vue'
const route=useRoute(); const order=ref(null); const driverLocation=ref(null); const loading=ref(false); const reviewing=ref(false); const review=reactive({rating:5,content:''})
async function load(){ loading.value=true; try{ order.value=await http.get(`/user/orders/${route.params.id}`); if(order.value.driverId) driverLocation.value=await http.get(`/common/driver-location/${order.value.driverId}`) } finally{ loading.value=false } }
async function cancel(){ order.value=await http.post(`/user/orders/${order.value.id}/cancel`,{reason:'用户主动取消'}); ElMessage.success('订单已取消') }
async function submitReview(){ reviewing.value=true; try{ await http.post('/user/reviews',{orderId:order.value.id,...review}); ElMessage.success('评价成功') } finally{ reviewing.value=false } }
onMounted(load)
</script>
<style scoped>.driver-card{display:flex;gap:14px;align-items:center}.avatar{width:54px;height:54px;border-radius:50%;background:var(--primary-soft);color:var(--primary);display:grid;place-items:center;font-weight:800;font-size:20px}</style>
