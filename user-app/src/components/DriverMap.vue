<template>
  <div :id="mapId" class="map">
    <div v-if="!ready" class="map-placeholder">{{ location ? '地图加载中' : '暂无司机位置，上报后显示' }}</div>
  </div>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

const props = defineProps({ location: Object })
const ready = ref(false)
const mapId = `driver-map-${Math.random().toString(36).slice(2)}`
let map, marker

async function ensureMap() {
  if (!props.location?.lng || map) return
  await nextTick()
  const key = import.meta.env.VITE_AMAP_WEB_KEY
  const securityJsCode = import.meta.env.VITE_AMAP_SECURITY_JS_CODE
  if (securityJsCode) window._AMapSecurityConfig = { securityJsCode }
  if (!key || key.startsWith('your_')) return
  try {
    const AMap = await AMapLoader.load({ key, version: '2.0', plugins: ['AMap.ToolBar'] })
    ready.value = true
    const pos = [Number(props.location.lng), Number(props.location.lat)]
    map = new AMap.Map(mapId, { viewMode: '2D', zoom: 14, center: pos })
    map.addControl(new AMap.ToolBar())
    marker = new AMap.Marker({
      position: pos,
      content: '<div style="width:34px;height:34px;border-radius:50%;background:#ff6b35;color:white;display:grid;place-items:center;font-weight:800;box-shadow:0 4px 10px rgba(0,0,0,.25)">车</div>',
      anchor: 'center'
    })
    map.add(marker)
  } catch (e) {
    ready.value = false
  }
}

watch(() => props.location, async val => {
  if (!val?.lng) return
  if (!map) await ensureMap()
  const pos = [Number(val.lng), Number(val.lat)]
  marker?.setPosition(pos)
  map?.setCenter(pos)
}, { immediate: true, deep: true })
</script>
