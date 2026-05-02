<template>
  <div class="page payment-page">
    <div class="card payment-card">
      <h1>确认支付</h1>
      <el-skeleton :loading="loading" animated :rows="6">
        <template v-if="order">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="起点">{{ order.startAddress }}</el-descriptions-item>
            <el-descriptions-item label="终点">{{ order.endAddress }}</el-descriptions-item>
            <el-descriptions-item label="车型">{{ order.vehicleTypeName }}</el-descriptions-item>
            <el-descriptions-item label="最终费用"><span class="money">￥{{ order.finalAmount }}</span></el-descriptions-item>
          </el-descriptions>
          <h2>选择支付方式</h2>
          <el-radio-group v-model="paymentMethod" class="pay-methods">
            <el-radio-button label="WECHAT_PAY">微信支付</el-radio-button>
            <el-radio-button label="ALIPAY">支付宝</el-radio-button>
          </el-radio-group>
          <div v-if="result" class="pay-result" :class="result.ok?'success':'fail'">{{ result.message }}</div>
          <div class="action-bar">
            <el-button @click="$router.back()">取消</el-button>
            <el-button type="primary" :loading="paying" @click="confirmPay">确认支付</el-button>
          </div>
        </template>
      </el-skeleton>
    </div>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
const route=useRoute(); const router=useRouter(); const order=ref(null); const loading=ref(false); const paying=ref(false); const paymentMethod=ref('WECHAT_PAY'); const result=ref(null)
async function load(){ loading.value=true; try{ order.value=await http.get(`/user/orders/${route.params.orderId}`) } finally{ loading.value=false } }
async function confirmPay(){ paying.value=true; result.value=null; try{ await http.post(`/user/order/${order.value.id}/pay`,{paymentMethod:paymentMethod.value}); result.value={ok:true,message:'支付成功，订单已完成'}; ElMessage.success('支付成功'); setTimeout(()=>router.push(`/orders/${order.value.id}`),700) } catch(e){ result.value={ok:false,message:e.message||'支付失败'} } finally{ paying.value=false } }
onMounted(load)
</script>
<style scoped>.payment-page{display:grid;place-items:start center}.payment-card{width:min(760px,100%)}.pay-methods{margin:12px 0 18px}.pay-result{padding:12px;border-radius:12px;margin:12px 0}.pay-result.success{background:#ecfdf5;color:#047857}.pay-result.fail{background:#fef2f2;color:#dc2626}</style>
