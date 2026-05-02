<template>
  <div class="page">
    <div class="toolbar" style="justify-content:space-between">
      <h1>投诉处理</h1>
      <div style="display:flex;gap:10px;align-items:center">
        <el-select v-model="status" clearable style="width:200px" @change="load">
          <el-option label="待处理" value="OPEN"/>
          <el-option label="处理中" value="IN_PROGRESS"/>
          <el-option label="已解决" value="RESOLVED"/>
          <el-option label="已关闭" value="CLOSED"/>
        </el-select>
        <el-button @click="load">刷新</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="rows" border>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="orderId" label="订单" width="90"/>
        <el-table-column prop="userName" label="用户" width="140"/>
        <el-table-column prop="driverName" label="司机" width="140"/>
        <el-table-column prop="title" label="标题" min-width="160"/>
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip/>
        <el-table-column label="投诉凭证" width="100">
          <template #default="{ row }">
            <el-button v-if="row.imageUrl" link type="primary" @click="openImage(row.imageUrl)">查看</el-button>
            <span v-else class="muted">无</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="120">
          <template #default="{ row }">{{ orderStatusCn(row.orderStatus) }}</template>
        </el-table-column>
        <el-table-column label="投诉状态" width="120">
          <template #default="{ row }">{{ complaintStatusCn(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="处理说明" min-width="220" show-overflow-tooltip/>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button @click="open(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" title="处理投诉" width="640px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="待处理" value="OPEN"/>
            <el-option label="处理中" value="IN_PROGRESS"/>
            <el-option label="已解决" value="RESOLVED"/>
            <el-option label="已关闭" value="CLOSED"/>
          </el-select>
        </el-form-item>
        <el-form-item label="处理措施">
          <el-radio-group v-model="form.actionType">
            <el-radio label="NONE">无</el-radio>
            <el-radio label="DEDUCT">扣薪处理</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.actionType==='DEDUCT'" label="扣薪金额">
          <el-input-number v-model="form.penaltyAmount" :min="0" :step="10" />
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="form.adminRemark" type="textarea" :rows="4"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { complaintStatusCn, orderStatusCn } from '../utils/orderStatus'

const rows = ref([])
const status = ref('')
const visible = ref(false)
const saving = ref(false)
const currentId = ref(null)
const form = reactive({ status: 'IN_PROGRESS', adminRemark: '', actionType: 'NONE', penaltyAmount: 0 })

async function load() {
  rows.value = await http.get('/admin/complaints', { params: { status: status.value } })
}

function open(row) {
  currentId.value = row.id
  Object.assign(form, {
    status: row.status || 'IN_PROGRESS',
    adminRemark: row.adminRemark || '',
    actionType: row.actionType || 'NONE',
    penaltyAmount: Number(row.penaltyAmount || 0)
  })
  visible.value = true
}

function openImage(u) {
  if (!u) return
  if (u.startsWith('http')) return window.open(u, '_blank')
  const base = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/api\/?$/, '')
  window.open(base + u, '_blank')
}

async function save() {
  saving.value = true
  try {
    await http.put(`/admin/complaints/${currentId.value}`, { ...form })
    ElMessage.success('已更新')
    visible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
