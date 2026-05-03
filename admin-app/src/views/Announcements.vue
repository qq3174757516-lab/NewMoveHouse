<template>
  <div class="page">
    <div class="toolbar" style="justify-content:space-between">
      <h1>系统公告</h1>
      <el-button type="primary" @click="openCreate">发布公告</el-button>
    </div>
    <div class="card">
      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="受众" width="120">
          <template #default="{ row }">
            <el-tag :type="row.audience==='USER'?'success':'warning'">{{ row.audience==='USER'?'用户':'司机' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="content" label="内容" min-width="320" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled?'success':'info'">{{ row.enabled?'启用':'禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.enabled" type="success" link @click="toggleEnabled(row, 1)">启用</el-button>
            <el-button v-else type="warning" link @click="toggleEnabled(row, 0)">禁用</el-button>
            <el-button @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.id?'编辑公告':'发布公告'" width="720px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="受众">
          <el-radio-group v-model="form.audience">
            <el-radio-button label="USER">用户端</el-radio-button>
            <el-radio-button label="DRIVER">司机端</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'

const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const form = reactive({ id: null, audience: 'USER', title: '', content: '', enabled: 1 })

function reset() {
  Object.assign(form, { id: null, audience: 'USER', title: '', content: '', enabled: 1 })
}

async function load() {
  loading.value = true
  try {
    rows.value = await http.get('/admin/announcements')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  reset()
  visible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  visible.value = true
}

async function save() {
  saving.value = true
  try {
    await http.post('/admin/announcements', form)
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除公告「${row.title}」？`, '删除确认', { type: 'warning' })
  await http.delete(`/admin/announcements/${row.id}`)
  ElMessage.success('删除成功')
  load()
}

async function toggleEnabled(row, enabled) {
  await http.post(`/admin/announcements/${row.id}/enabled`, { enabled })
  ElMessage.success(enabled === 1 ? '已启用' : '已禁用')
  load()
}

onMounted(load)
</script>
