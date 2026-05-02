<template>
  <div :id="mapId" class="map">
    <div v-if="!ready" class="map-placeholder">
      <template v-if="loadError">{{ loadError }}</template>
      <template v-else-if="hasLocation">地图加载中…</template>
      <template v-else>暂无司机位置，司机上报后显示</template>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { loadAmap } from '../utils/amapConfig'

const props = defineProps({ location: Object })
const ready = ref(false)
const loadError = ref('')
const mapId = `driver-map-${Math.random().toString(36).slice(2)}`
let map, marker

const hasLocation = computed(() => {
  const loc = props.location
  if (!loc || loc.lng == null || loc.lat == null) return false
  const lng = Number(loc.lng)
  const lat = Number(loc.lat)
  return !Number.isNaN(lng) && !Number.isNaN(lat)
})

async function ensureMap() {
  loadError.value = ''
  if (map || !hasLocation.value) return
  await nextTick()
  try {
    const AMap = await loadAmap(['AMap.ToolBar'])
    const pos = [Number(props.location.lng), Number(props.location.lat)]
    map = new AMap.Map(mapId, { viewMode: '2D', zoom: 14, center: pos })
    map.addControl(new AMap.ToolBar())
    marker = new AMap.Marker({
      position: pos,
      content:
        '<div style="width:34px;height:34px;border-radius:50%;background:#ff6b35;color:white;display:grid;place-items:center;font-weight:800;box-shadow:0 4px 10px rgba(0,0,0,.25)">车</div>',
      anchor: 'center'
    })
    map.add(marker)
    ready.value = true
  } catch (e) {
    loadError.value = e?.message || '地图加载失败'
    ready.value = false
  }
}

watch(
  () => props.location,
  async val => {
    if (!hasLocation.value) {
      ready.value = false
      loadError.value = ''
      return
    }
    if (!map) {
      await ensureMap()
      return
    }
    if (!marker) return
    const pos = [Number(val.lng), Number(val.lat)]
    marker.setPosition(pos)
    map.setCenter(pos)
    ready.value = true
  },
  { immediate: true, deep: true }
)
</script>

<style scoped>
.map {
  position: relative;
  width: 100%;
  min-height: 220px;
  border-radius: 12px;
  overflow: hidden;
  background: #f3f4f6;
}
.map-placeholder {
  position: absolute;
  inset: 0;
  z-index: 1;
  display: grid;
  place-items: center;
  padding: 12px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
  background: #f3f4f6;
}
</style>
