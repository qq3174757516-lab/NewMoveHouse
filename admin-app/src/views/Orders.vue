<template>
  <div class="page">
    <div class="toolbar">
      <h1>订单管理</h1>
      <el-select v-model="status" clearable placeholder="全部状态" style="width:200px" @change="load">
        <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value"/>
      </el-select>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="rows" border>
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="userName" label="用户"/>
      <el-table-column prop="driverName" label="司机"/>
      <el-table-column prop="startAddress" label="起点"/>
      <el-table-column prop="endAddress" label="终点"/>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">{{ orderStatusCn(row.status) }}</template>
      </el-table-column>
      <el-table-column prop="finalAmount" label="金额"/>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button type="danger" @click="cancel(row)">强制取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'
import { ORDER_STATUS_CN, orderStatusCn } from '../utils/orderStatus'

const status = ref('')
const rows = ref([])

const statusOptions = [
  { value: '', label: '全部' },
  ...Object.keys(ORDER_STATUS_CN).map(k => ({ value: k, label: ORDER_STATUS_CN[k] }))
]

async function load() {
  rows.value = await http.get('/admin/orders', { params: { status: status.value || undefined } })
}

async function cancel(r) {
  await http.post(`/admin/orders/${r.id}/cancel`, { reason: '管理员强制取消' })
  load()
}

onMounted(load)
</script>
