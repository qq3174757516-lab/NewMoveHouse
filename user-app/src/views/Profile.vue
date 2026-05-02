<template>
  <div class="page">
    <h1>个人中心</h1>
    <div class="grid">
      <div class="card">
        <div class="toolbar" style="justify-content:space-between;margin:0 0 12px">
          <h2>基本信息</h2>
          <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</el-button>
        </div>
        <el-form :model="profile" label-width="90px">
          <el-form-item label="用户名"><el-input v-model="me.username" disabled /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="profile.phone" autocomplete="off" /></el-form-item>
          <el-form-item label="昵称"><el-input v-model="profile.nickname" autocomplete="off" /></el-form-item>
        </el-form>
      </div>

      <div class="card">
        <div class="toolbar" style="justify-content:space-between;margin:0 0 12px">
          <h2>常用地址</h2>
          <el-button type="primary" @click="openAddr()">新增地址</el-button>
        </div>
        <el-table :data="addresses" border>
          <el-table-column prop="name" label="名称" width="120" />
          <el-table-column prop="detail" label="地址" min-width="220" />
          <el-table-column label="坐标" min-width="200">
            <template #default="{ row }">{{ row.lng }}, {{ row.lat }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openAddr(row)">编辑</el-button>
              <el-button link type="danger" @click="removeAddr(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="addrVisible" :title="addr.id ? '编辑地址' : '新增地址'" width="640px">
      <el-form :model="addr" label-width="90px">
        <el-form-item label="名称"><el-input v-model="addr.name" autocomplete="off" /></el-form-item>
        <el-form-item label="详情">
          <el-input v-model="addr.detail" type="textarea" autocomplete="off" />
        </el-form-item>
        <el-form-item label="地图">
          <MapPicker label="地图选点" @pick="onPick" />
        </el-form-item>
        <el-form-item label="坐标">
          <el-input v-model="addrText" placeholder="也可手动输入：lng,lat" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addrVisible=false">取消</el-button>
        <el-button type="primary" :loading="savingAddr" @click="saveAddr">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import MapPicker from '../components/MapPicker.vue'

const me = ref({ username: '' })
const profile = reactive({ phone: '', nickname: '' })
const savingProfile = ref(false)

const addresses = ref([])
const addrVisible = ref(false)
const savingAddr = ref(false)
const addrText = ref('')
const addr = reactive({ id: null, name: '', detail: '', lng: null, lat: null })

async function load() {
  me.value = await http.get('/user/me')
  profile.phone = me.value.phone || ''
  profile.nickname = me.value.nickname || ''
  addresses.value = await http.get('/user/addresses')
}

async function saveProfile() {
  savingProfile.value = true
  try {
    await http.put('/user/profile', { phone: profile.phone, nickname: profile.nickname })
    await load()
    ElMessage.success('资料已更新')
  } finally {
    savingProfile.value = false
  }
}

function openAddr(row) {
  if (row) {
    Object.assign(addr, { id: row.id, name: row.name, detail: row.detail, lng: row.lng, lat: row.lat })
    addrText.value = `${row.lng},${row.lat}`
  } else {
    Object.assign(addr, { id: null, name: '', detail: '', lng: null, lat: null })
    addrText.value = ''
  }
  addrVisible.value = true
}

function onPick(p) {
  addr.lng = p.lng
  addr.lat = p.lat
  addrText.value = `${p.lng},${p.lat}`
  if (!addr.detail) addr.detail = p.address || addr.detail
}

async function saveAddr() {
  const parts = addrText.value.split(',').map(s => s.trim())
  if (parts.length !== 2) {
    ElMessage.warning('坐标格式应为：lng,lat')
    return
  }
  const lng = Number(parts[0])
  const lat = Number(parts[1])
  if (!Number.isFinite(lng) || !Number.isFinite(lat)) {
    ElMessage.warning('坐标不合法')
    return
  }
  savingAddr.value = true
  try {
    await http.post('/user/addresses', { ...addr, lng, lat })
    ElMessage.success('地址已保存')
    addrVisible.value = false
    await load()
  } finally {
    savingAddr.value = false
  }
}

async function removeAddr(row) {
  await ElMessageBox.confirm(`确认删除地址「${row.name}」？`, '删除确认', { type: 'warning' })
  await http.delete(`/user/addresses/${row.id}`)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>
