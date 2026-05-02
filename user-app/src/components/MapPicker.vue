<template>
  <span>
    <el-button :icon="Location" @click="visible=true">{{ label || '地图选点' }}</el-button>
    <el-dialog v-model="visible" title="地图选点" width="860px" @opened="initMap">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索地点，例如：长沙站" @keyup.enter="search" />
        <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
      </div>
      <div :id="mapId" class="map"><div v-if="!mapReady" class="map-placeholder">地图加载中，请确认高德 Key 与安全密钥配置正确</div></div>
      <div class="toolbar" style="margin-top:14px">
        <span class="muted">已选：{{ picked.address || '点击地图选择地址' }}</span>
        <el-button type="primary" :disabled="!picked.lng" @click="confirm">确认选择</el-button>
      </div>
    </el-dialog>
  </span>
</template>

<script setup>
import { nextTick, reactive, ref } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { ElMessage } from 'element-plus'
import { Location, Search } from '@element-plus/icons-vue'

const props = defineProps({ label: String })
const emit = defineEmits(['pick'])
const visible = ref(false)
const keyword = ref('')
const mapReady = ref(false)
const mapId = `amap-picker-${Math.random().toString(36).slice(2)}`
const picked = reactive({ lng: null, lat: null, address: '' })
let map, marker, geocoder, placeSearch

async function loadAmap() {
  const key = import.meta.env.VITE_AMAP_WEB_KEY
  const securityJsCode = import.meta.env.VITE_AMAP_SECURITY_JS_CODE
  if (securityJsCode) window._AMapSecurityConfig = { securityJsCode }
  if (!key || key.startsWith('your_')) throw new Error('未配置 VITE_AMAP_WEB_KEY')
  return AMapLoader.load({ key, version: '2.0', plugins: ['AMap.Geocoder', 'AMap.PlaceSearch', 'AMap.ToolBar'] })
}

async function initMap() {
  if (map) return
  await nextTick()
  try {
    const AMap = await loadAmap()
    mapReady.value = true
    map = new AMap.Map(mapId, { viewMode: '2D', zoom: 12, center: [112.938882, 28.228304] })
    map.addControl(new AMap.ToolBar())
    marker = new AMap.Marker({ anchor: 'bottom-center' })
    geocoder = new AMap.Geocoder()
    placeSearch = new AMap.PlaceSearch({ city: '全国', pageSize: 6 })
    map.on('click', e => choose(AMap, e.lnglat.lng, e.lnglat.lat))
  } catch (e) {
    ElMessage.warning(e.message || '地图加载失败，可手动输入坐标')
  }
}

function choose(AMap, lng, lat, address) {
  picked.lng = lng
  picked.lat = lat
  marker.setPosition([lng, lat])
  map.add(marker)
  map.setCenter([lng, lat])
  if (address) {
    picked.address = address
    return
  }
  geocoder.getAddress([lng, lat], (status, result) => {
    picked.address = status === 'complete' ? result.regeocode.formattedAddress : `${lng},${lat}`
  })
}

function search() {
  if (!placeSearch || !keyword.value) return
  placeSearch.search(keyword.value, (status, result) => {
    const poi = result?.poiList?.pois?.[0]
    if (!poi) return ElMessage.warning('未找到地点')
    choose(window.AMap, poi.location.lng, poi.location.lat, poi.name)
  })
}

function confirm() {
  emit('pick', { ...picked })
  visible.value = false
}
</script>

