<template>
  <div class="page">
    <div class="toolbar">
      <h1>用户管理</h1>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">{{ userAccountStatusCn(row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button @click="setStatus(row, row.status ? 0 : 1)">{{ row.status ? '禁用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'
import { userAccountStatusCn } from '../utils/orderStatus'

const rows = ref([])

async function load() {
  rows.value = await http.get('/admin/users')
}

async function setStatus(r, s) {
  await http.post(`/admin/users/${r.id}/status`, { status: s })
  load()
}

onMounted(load)
</script>
