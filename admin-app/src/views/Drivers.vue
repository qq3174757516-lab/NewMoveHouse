<template>
  <div class="page">
    <div class="toolbar">
      <h1>司机审核</h1>
      <div style="display:flex;gap:10px;align-items:center">
        <el-select v-model="status" clearable @change="load" style="width:180px">
          <el-option label="待审核" value="PENDING"/>
          <el-option label="已通过" value="APPROVED"/>
          <el-option label="已拒绝" value="REJECTED"/>
          <el-option label="已停用" value="DISABLED"/>
        </el-select>
        <el-button @click="load">刷新</el-button>
      </div>
    </div>

    <el-table :data="rows" border>
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="realName" label="姓名"/>
      <el-table-column prop="phone" label="电话"/>
      <el-table-column prop="vehiclePlate" label="车牌"/>
      <el-table-column prop="vehicleTypeName" label="车型"/>
      <el-table-column prop="auditStatus" label="审核"/>
      <el-table-column label="操作" width="360" fixed="right">
        <template #default="{row}">
          <el-button @click="openDocs(row)">资料</el-button>
          <el-button type="success" @click="audit(row,'APPROVED')">通过</el-button>
          <el-button type="warning" @click="audit(row,'REJECTED')">拒绝</el-button>
          <el-button type="danger" @click="audit(row,'DISABLED')">停用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="docVisible" title="司机资质材料" width="860px">
      <el-table :data="docs" border>
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ row.docType === 'INSURANCE' ? '保险' : '驾驶证' }}</template>
        </el-table-column>
        <el-table-column prop="originalName" label="文件名" min-width="220"/>
        <el-table-column prop="createdAt" label="上传时间" width="180"/>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openFile(row.fileUrl)">打开</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const rows = ref([])
const status = ref('')
const docs = ref([])
const docVisible = ref(false)

function fileUrlToAbs(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const apiBase = import.meta.env.VITE_API_BASE_URL || ''
  if (apiBase) {
    const origin = apiBase.replace(/\/api\/?$/, '')
    return origin + url
  }
  return `${location.protocol}//${location.host}` + url
}

async function load() {
  rows.value = await http.get('/admin/drivers', { params: { auditStatus: status.value } })
}

async function audit(r, s) {
  await http.put(`/admin/driver/${r.id}/audit`, { status: s, reason: '后台操作' })
  ElMessage.success('审核状态已更新')
  load()
}

async function openDocs(row) {
  docVisible.value = true
  docs.value = await http.get(`/admin/drivers/${row.id}/documents`)
}

function openFile(u) {
  window.open(fileUrlToAbs(u), '_blank')
}

onMounted(load)
</script>
