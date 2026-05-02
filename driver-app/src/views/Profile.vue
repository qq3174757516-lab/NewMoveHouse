<template>
  <div class="page">
    <h1>个人资料</h1>

    <div class="card">
      <div class="toolbar" style="justify-content:space-between;margin:0 0 12px">
        <h2>基本信息</h2>
        <el-button type="primary" :loading="saving" @click="save">保存修改</el-button>
      </div>
      <el-form v-if="form" :model="form" label-width="100px">
        <el-form-item label="姓名"><el-input v-model="form.realName" autocomplete="off" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" autocomplete="off" /></el-form-item>
        <el-form-item label="审核状态"><el-tag>{{ me?.auditStatus }}</el-tag></el-form-item>
        <el-form-item label="车型">
          <el-select v-model="form.vehicleTypeId" style="width:100%">
            <el-option v-for="v in vehicles" :key="v.id" :label="v.name" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="车牌"><el-input v-model="form.vehiclePlate" autocomplete="off" /></el-form-item>
        <el-form-item label="服务区域"><el-input v-model="form.serviceArea" autocomplete="off" /></el-form-item>
      </el-form>
    </div>

    <div class="card">
      <h2>资质材料</h2>
      <p class="muted">请上传清晰的保险信息与驾驶证信息（支持图片/PDF）。管理员审核可在后台查看。</p>
      <div class="toolbar" style="gap:12px;flex-wrap:wrap">
        <el-upload :show-file-list="false" :http-request="(o)=>uploadDoc('INSURANCE', o)">
          <el-button type="primary">上传保险信息</el-button>
        </el-upload>
        <el-upload :show-file-list="false" :http-request="(o)=>uploadDoc('LICENSE', o)">
          <el-button>上传驾驶证信息</el-button>
        </el-upload>
      </div>
      <el-table :data="docs" style="margin-top:12px" border>
        <el-table-column prop="docType" label="类型" width="140">
          <template #default="{ row }">{{ row.docType === 'INSURANCE' ? '保险' : '驾驶证' }}</template>
        </el-table-column>
        <el-table-column prop="originalName" label="文件名" min-width="220" />
        <el-table-column prop="createdAt" label="上传时间" width="180" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openFile(row.fileUrl)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const me = ref(null)
const vehicles = ref([])
const docs = ref([])
const saving = ref(false)

const form = reactive({
  realName: '',
  phone: '',
  vehicleTypeId: null,
  vehiclePlate: '',
  serviceArea: ''
})

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
  me.value = await http.get('/driver/me')
  vehicles.value = await http.get('/common/vehicle-types')
  docs.value = await http.get('/driver/documents')
  Object.assign(form, {
    realName: me.value.realName || '',
    phone: me.value.phone || '',
    vehicleTypeId: me.value.vehicleTypeId,
    vehiclePlate: me.value.vehiclePlate || '',
    serviceArea: me.value.serviceArea || ''
  })
}

async function save() {
  saving.value = true
  try {
    await http.put('/driver/profile', { ...form })
    ElMessage.success('已保存')
    await load()
  } finally {
    saving.value = false
  }
}

async function uploadDoc(docType, opt) {
  const fd = new FormData()
  fd.append('file', opt.file)
  const up = await http.post('/upload', fd)
  await http.post('/driver/documents', { docType, fileUrl: up.fileUrl, originalName: up.originalName || opt.file?.name })
  ElMessage.success('上传成功')
  await load()
}

function openFile(u) {
  window.open(fileUrlToAbs(u), '_blank')
}

onMounted(load)
watch(() => auth.auditStatus, s => { if (me.value) me.value.auditStatus = s })
</script>
