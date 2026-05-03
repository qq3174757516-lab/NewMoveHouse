<template>
  <span>
    <el-button :icon="Location" @click="visible=true">{{ label || '地图选点' }}</el-button>
    <el-dialog v-model="visible" title="地图选点" width="860px" @opened="initMap">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索地点，例如：长沙站" @keyup.enter="search" />
        <el-button type="primary" :icon="Search" :loading="searching" @click="search">搜索</el-button>
        <el-button :icon="Aim" @click="locateCurrent">当前位置</el-button>
      </div>
      <div :id="mapId" class="map-wrap">
        <div v-if="!mapReady" class="map-placeholder">地图加载中…</div>
      </div>
      <div class="toolbar" style="margin-top:14px">
        <span class="muted">已选：{{ picked.address || '点击地图选择地址' }}</span>
        <el-button type="primary" :disabled="!picked.lng || resolving" :loading="resolving" @click="confirm">确认选择</el-button>
      </div>
    </el-dialog>
  </span>
</template>

<script setup>
import { nextTick, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Aim, Location, Search } from '@element-plus/icons-vue'
import { apiBase, loadAmap } from '../utils/amapConfig'

const props = defineProps({ label: String })
const emit = defineEmits(['pick'])
const visible = ref(false)
const keyword = ref('')
const mapReady = ref(false)
const resolving = ref(false)
const searching = ref(false)
const mapId = `amap-picker-${Math.random().toString(36).slice(2)}`
const picked = reactive({ lng: null, lat: null, address: '' })
let map, marker
let AMapNs = null

/**
 * 逆地理：走服务端 REST（/v3/geocode/regeo），不依赖浏览器里的 Geocoder 插件，
 * 可避免 JSAPI 安全校验报错 INVALID_USER_SCODE。
 */
async function reverseGeocode(lng, lat) {
  try {
    const res = await fetch(
      `${apiBase()}/common/amap/regeo?lng=${encodeURIComponent(lng)}&lat=${encodeURIComponent(lat)}`
    )
    const json = await res.json()
    if (json.code !== 0 || !json.data) {
      return `${lng},${lat}`
    }
    const d = json.data
    if (d.success && d.displayAddress) {
      return String(d.displayAddress)
    }
    if (!d.success && d.message) {
      ElMessage.warning(String(d.message))
    }
    return `${lng},${lat}`
  } catch (e) {
    ElMessage.warning('逆地理请求失败，请检查后端 amap.server-key 是否具备逆地理权限')
    return `${lng},${lat}`
  }
}

async function initMap() {
  if (map) return
  await nextTick()
  try {
    // 仅加载地图与 ToolBar；PlaceSearch/Geocoder 在开启安全密钥后常在浏览器侧报 INVALID_USER_SCODE，地名改由服务端 REST 解析
    AMapNs = await loadAmap(['AMap.ToolBar'])
    mapReady.value = true
    map = new AMapNs.Map(mapId, { viewMode: '2D', zoom: 12, center: [112.938882, 28.228304] })
    map.addControl(new AMapNs.ToolBar())
    marker = new AMapNs.Marker({ anchor: 'bottom-center' })
    map.on('click', async e => {
      await choose(e.lnglat.lng, e.lnglat.lat, null)
    })
  } catch (e) {
    ElMessage.warning(e.message || '地图加载失败，可手动输入坐标')
  }
}

async function choose(lng, lat, addressHint) {
  if (!map || !marker) return
  picked.lng = lng
  picked.lat = lat
  marker.setPosition([lng, lat])
  map.add(marker)
  map.setCenter([lng, lat])
  if (addressHint && String(addressHint).trim()) {
    picked.address = String(addressHint).trim()
    return
  }
  resolving.value = true
  try {
    picked.address = await reverseGeocode(lng, lat)
  } finally {
    resolving.value = false
  }
}

/** 关键字搜索：服务端 /v3/place/text */
async function search() {
  if (!keyword.value.trim()) return
  if (!map || !marker) {
    ElMessage.warning('请等待地图加载完成后再搜索')
    return
  }
  searching.value = true
  try {
    const res = await fetch(
      `${apiBase()}/common/amap/place-text?keywords=${encodeURIComponent(keyword.value.trim())}`
    )
    const json = await res.json()
    if (json.code !== 0 || !json.data) {
      ElMessage.warning('搜索请求失败')
      return
    }
    const d = json.data
    if (!d.success) {
      ElMessage.warning(d.message ? String(d.message) : '未找到地点')
      return
    }
    const lng = Number(d.lng)
    const lat = Number(d.lat)
    const hint = d.displayAddress ? String(d.displayAddress) : ''
    await choose(lng, lat, hint || null)
  } catch (e) {
    ElMessage.warning('搜索失败，请检查后端 Key 是否开通关键字搜索')
  } finally {
    searching.value = false
  }
}

function locateCurrent() {
  if (!navigator.geolocation) return ElMessage.warning('当前浏览器不支持定位')
  if (!map || !marker) {
    ElMessage.warning('请等待地图加载完成')
    return
  }
  navigator.geolocation.getCurrentPosition(
    async p => {
      await choose(p.coords.longitude, p.coords.latitude, null)
    },
    () => ElMessage.warning('定位失败，请检查浏览器定位权限'),
    { enableHighAccuracy: true, timeout: 6000 }
  )
}

async function confirm() {
  if (picked.lng == null || picked.lat == null) return
  resolving.value = true
  try {
    picked.address = await reverseGeocode(picked.lng, picked.lat)
    emit('pick', { ...picked })
    visible.value = false
  } finally {
    resolving.value = false
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.toolbar .el-input {
  flex: 1;
  min-width: 220px;
}
.muted {
  color: #6b7280;
  flex: 1;
}
.map-wrap {
  position: relative;
  width: 100%;
  height: 420px;
  border-radius: 12px;
  overflow: hidden;
  background: #f3f4f6;
}
.map-placeholder {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  color: #6b7280;
  font-size: 14px;
}
</style>
