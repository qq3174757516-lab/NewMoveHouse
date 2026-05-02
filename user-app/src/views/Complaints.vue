<template>
  <div class="page">
    <div class="toolbar" style="justify-content:space-between">
      <h1>投诉与售后</h1>
      <el-button type="primary" @click="openCreate">发起投诉</el-button>
    </div>

    <div class="card">
      <el-table :data="rows" border>
        <el-table-column prop="orderId" label="订单" width="100" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column label="凭证图片" width="100">
          <template #default="{ row }">
            <el-button v-if="row.imageUrl" link type="primary" @click="openImage(row.imageUrl)">查看</el-button>
            <span v-else class="muted">无</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ complaintStatusCn(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="处理说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="提交时间" width="180" />
      </el-table>
    </div>

    <el-dialog v-model="visible" title="发起投诉" width="720px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="订单">
          <el-select v-model="form.orderId" filterable placeholder="请选择已完成订单" style="width:100%">
            <el-option v-for="o in completedOrders" :key="o.id" :label="`#${o.id} ${o.startAddress}→${o.endAddress}`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="投诉图片">
          <el-upload :show-file-list="false" :http-request="uploadImage">
            <el-button>上传图片</el-button>
          </el-upload>
          <el-link v-if="form.imageUrl" type="primary" style="margin-left:10px" @click="openImage(form.imageUrl)">已上传，点击查看</el-link>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { complaintStatusCn } from '../utils/orderStatus'

const rows = ref([])
const completedOrders = ref([])
const visible = ref(false)
const saving = ref(false)
const form = reactive({ orderId: null, title: '', content: '', imageUrl: '' })

function tagType(s) {
  if (s === 'RESOLVED') return 'success'
  if (s === 'CLOSED') return 'info'
  if (s === 'IN_PROGRESS') return 'warning'
  return 'danger'
}

async function load() {
  rows.value = await http.get('/user/complaints')
}

async function loadOrders() {
  completedOrders.value = await http.get('/user/orders', { params: { status: 'COMPLETED' } })
}

function openCreate() {
  Object.assign(form, { orderId: null, title: '', content: '', imageUrl: '' })
  visible.value = true
}

function absUrl(u) {
  if (!u) return ''
  if (u.startsWith('http')) return u
  const base = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/api\/?$/, '')
  return base + u
}

function openImage(u) {
  window.open(absUrl(u), '_blank')
}

async function uploadImage(opt) {
  const fd = new FormData()
  fd.append('file', opt.file)
  const r = await http.post('/upload', fd)
  form.imageUrl = r.fileUrl
  ElMessage.success('图片上传成功')
}

async function submit() {
  if (!form.orderId) return ElMessage.warning('请选择订单')
  if (!form.title || !form.content) return ElMessage.warning('请填写标题与内容')
  if (!form.imageUrl) return ElMessage.warning('请先上传投诉图片')
  saving.value = true
  try {
    await http.post('/user/complaints', { ...form })
    ElMessage.success('已提交')
    visible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await Promise.all([load(), loadOrders()])
})
</script>
