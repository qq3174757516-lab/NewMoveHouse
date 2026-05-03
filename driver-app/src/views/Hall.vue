<template>
  <div class="page">
    <section class="notice">
      <div class="toolbar" style="justify-content:space-between;margin:0">
        <div>
          <h1>接单大厅</h1>
          <p class="muted">{{ me?.auditStatus==='APPROVED' ? '新订单会实时推送到这里，准备好就立即抢单。' : '审核通过后即可接单，审核结果会实时通知。' }}</p>
        </div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
    </section>
    <el-alert v-if="me && me.auditStatus!=='APPROVED'" type="warning" :closable="false" :title="`当前审核状态：${auditStatusCn(me.auditStatus)}`"/>
    <el-skeleton :loading="loading" animated :rows="5">
      <el-empty v-if="orders.length===0" description="暂无待接订单" />
      <article v-for="row in orders" :key="row.id" class="order-card">
        <div class="toolbar" style="justify-content:space-between">
          <span class="route">{{ row.startAddress }} → {{ row.endAddress }}</span>
          <span class="money">￥{{ row.finalAmount }}</span>
        </div>
        <p class="muted">预约：{{ row.appointmentTime || '尽快上门' }} · 车型：{{ row.vehicleTypeName }} </p>
        <div class="action-bar"><el-button type="primary" :loading="accepting===row.id" @click="accept(row.id)">立即抢单</el-button></div>
      </article>
    </el-skeleton>
  </div>
</template>
<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'
import { auditStatusCn } from '../utils/orderStatus'
const orders=ref([]); const me=ref(null); const loading=ref(false); const accepting=ref(null); const auth=useAuthStore()
async function load(){ loading.value=true; try{ me.value=await http.get('/driver/me'); auth.setProfile(me.value); if(me.value.auditStatus==='APPROVED') orders.value=await http.get('/driver/hall'); else orders.value=[] } finally{ loading.value=false } }
async function accept(id){ accepting.value=id; try{ await http.post(`/driver/orders/${id}/accept`); ElMessage.success('抢单成功'); load() } finally{ accepting.value=null } }
onMounted(load)
watch(() => auth.auditStatus, (next, prev) => {
  if (!next || next === prev) return
  // 审核状态通过 WebSocket 实时更新后，立即刷新大厅数据
  load()
})
</script>
