<template>
  <div class="page">
    <div class="toolbar" style="justify-content:space-between">
      <h1>用户投诉</h1>
      <el-button @click="load">刷新</el-button>
    </div>

    <div class="card">
      <el-table :data="rows" border>
        <el-table-column prop="orderId" label="订单" width="100" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ complaintStatusCn(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="平台处理" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'
import { complaintStatusCn } from '../utils/orderStatus'

const rows = ref([])

function tagType(s) {
  if (s === 'RESOLVED') return 'success'
  if (s === 'CLOSED') return 'info'
  if (s === 'IN_PROGRESS') return 'warning'
  return 'danger'
}

async function load() {
  rows.value = await http.get('/driver/complaints')
}

onMounted(load)
</script>
