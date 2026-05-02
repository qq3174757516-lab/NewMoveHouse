<template>
  <span>
    <el-button :icon="Location" @click="visible=true">{{ label || '地图选点' }}</el-button>
    <el-dialog v-model="visible" title="地图选点" width="860px" @opened="initMap">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索地点，例如：长沙站" @keyup.enter="search" />
        <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
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
import { loadAmap } from '../utils/amapConfig'

const props = defineProps({ label: String })
const emit = defineEmits(['pick'])
const visible = ref(false)
const keyword = ref('')
const mapReady = ref(false)
const resolving = ref(false)
const mapId = `amap-picker-${Math.random().toString(36).slice(2)}`
const picked = reactive({ lng: null, lat: null, address: '' })
let map, marker, geocoder, placeSearch
let AMapNs = null

/** 逆地理编码：高德会返回 formattedAddress、周边 POI 等，用于展示“地名”而非纯坐标 */
function reverseGeocode(lng, lat) {
  return new Promise(resolve => {
    if (!geocoder) {
      resolve(`${lng},${lat}`)
      return
    }
    geocoder.getAddress([lng, lat], (status, result) => {
      if (status === 'complete' && result?.regeocode) {
        const r = result.regeocode
        const formatted = r.formattedAddress
        const poiName = r.pois?.[0]?.name
        const name = [poiName, formatted].filter(Boolean).join(' · ')
        resolve(name || formatted || `${lng},${lat}`)
      } else {
        resolve(`${lng},${lat}`)
      }
    })
  })
}

async function initMap() {
  if (map) return
  await nextTick()
  try {
    AMapNs = await loadAmap(['AMap.Geocoder', 'AMap.PlaceSearch', 'AMap.ToolBar'])
    mapReady.value = true
    map = new AMapNs.Map(mapId, { viewMode: '2D', zoom: 12, center: [112.938882, 28.228304] })
    map.addControl(new AMapNs.ToolBar())
    marker = new AMapNs.Marker({ anchor: 'bottom-center' })
    geocoder = new AMapNs.Geocoder()
    placeSearch = new AMapNs.PlaceSearch({ city: '全国', pageSize: 10 })
    map.on('click', async e => {
      await choose(e.lnglat.lng, e.lnglat.lat, null)
    })
  } catch (e) {
    ElMessage.warning(e.message || '地图加载失败，可手动输入坐标')
  }
}

/**
 * 选点：若已有提示名（搜索/POI）则优先展示；否则等待逆地理编码完成后再写入地名。
 */
async function choose(lng, lat, addressHint) {
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

function search() {
  if (!placeSearch || !keyword.value) return
  placeSearch.search(keyword.value, async (status, result) => {
    const poi = result?.poiList?.pois?.[0]
    if (poi && AMapNs) {
      const name = [poi.name, poi.address].filter(Boolean).join(' ')
      await choose(poi.location.lng, poi.location.lat, name)
      return
    }
    if (!geocoder || !AMapNs) {
      ElMessage.warning('未找到地点')
      return
    }
    geocoder.getLocation(keyword.value, async (s, r) => {
      const loc = r?.geocodes?.[0]?.location
      if (!loc) {
        ElMessage.warning('未找到地点')
        return
      }
      const addr = r.geocodes[0].formattedAddress || keyword.value
      await choose(loc.lng, loc.lat, addr)
    })
  })
}

function locateCurrent() {
  if (!navigator.geolocation) return ElMessage.warning('当前浏览器不支持定位')
  navigator.geolocation.getCurrentPosition(
    async p => {
      await choose(p.coords.longitude, p.coords.latitude, null)
    },
    () => ElMessage.warning('定位失败，请检查浏览器定位权限'),
    { enableHighAccuracy: true, timeout: 6000 }
  )
}

/** 确认前再次逆地理，避免异步回调未完成时用户快速点击确认导致仍显示经纬度 */
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
