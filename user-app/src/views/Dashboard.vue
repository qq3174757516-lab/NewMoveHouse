<template>
  <div class="page">
    <section class="hero">
      <div>
        <h1>省心搬家，准时上门</h1>
        <p>地图选点、自动估价、司机实时接单，一次完成预约。</p>
      </div>
      <el-button size="large" color="#ffffff" :icon="Van" @click="scrollToForm">立即下单</el-button>
    </section>

    <div ref="formRef" class="card">
      <el-steps :active="step" finish-status="success" align-center>
        <el-step title="选择车型" />
        <el-step title="填写地址" />
        <el-step title="确认订单" />
      </el-steps>

      <h2>选择车型</h2>
      <el-skeleton :loading="loading" animated :rows="3">
        <div class="vehicle-grid">
          <div v-for="v in vehicles" :key="v.id" class="vehicle-card" :class="{active: form.vehicleTypeId===v.id}" @click="selectVehicle(v)">
            <div class="vehicle-icon"><el-icon><Van /></el-icon></div>
            <h3>{{ v.name }}</h3>
            <p class="muted">{{ v.description || '专业搬家车型' }}</p>
            <b>￥{{ v.basePrice }} 起 · ￥{{ v.perKmPrice }}/km</b>
          </div>
        </div>
      </el-skeleton>

      <h2>地址与服务信息</h2>
      <el-form label-width="110px" :model="form">
        <el-form-item label="起点">
          <el-input v-model="form.startAddress" placeholder="请选择起点或手动输入">
            <template #append><MapPicker label="地图选点" @pick="p=>pick('start',p)" /></template>
          </el-input>
        </el-form-item>
        <el-form-item label="终点">
          <el-input v-model="form.endAddress" placeholder="请选择终点或手动输入">
            <template #append><MapPicker label="地图选点" @pick="p=>pick('end',p)" /></template>
          </el-input>
        </el-form-item>
        <div class="grid">
          <el-form-item label="起点楼层"><el-input-number v-model="form.startFloor" :min="1" /> <el-checkbox v-model="form.startHasElevator">有电梯</el-checkbox></el-form-item>
          <el-form-item label="终点楼层"><el-input-number v-model="form.endFloor" :min="1" /> <el-checkbox v-model="form.endHasElevator">有电梯</el-checkbox></el-form-item>
        </div>
        <div class="grid">
          <el-form-item label="大件数量"><el-input-number v-model="form.largeItemCount" :min="0" /></el-form-item>
          <el-form-item label="预约时间"><el-date-picker v-model="form.appointmentTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="不选则尽快上门" /></el-form-item>
        </div>
        <div class="grid">
          <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
          <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
        </div>
        <el-form-item label="物品描述"><el-input v-model="form.itemDescription" type="textarea" /></el-form-item>
      </el-form>

      <div class="fee-panel">
        <div>
          <span class="muted">预估费用</span>
          <div class="money">￥{{ fee?.estimatedAmount || '--' }}</div>
          <p v-if="fee" class="muted">距离 {{ fee.distanceKm }} km，里程费 ￥{{ fee.mileageFee }}，楼层费 ￥{{ fee.floorFee }}，大件费 ￥{{ fee.largeItemFee }}，夜间费 ￥{{ fee.nightFee }}</p>
        </div>
        <div class="action-bar">
          <el-button :loading="estimating" @click="estimate">重新估价</el-button>
          <el-button type="primary" :loading="submitting" @click="create">提交订单</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Van } from '@element-plus/icons-vue'
import http from '../api/http'
import MapPicker from '../components/MapPicker.vue'

const router = useRouter()
const vehicles = ref([])
const fee = ref(null)
const loading = ref(false)
const estimating = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  vehicleTypeId: null,
  startAddress: '长沙站',
  startLng: 112.938882,
  startLat: 28.228304,
  endAddress: '五一广场',
  endLng: 112.982279,
  endLat: 28.19409,
  itemDescription: '纸箱和家具',
  startHasElevator: true,
  endHasElevator: true,
  startFloor: 1,
  endFloor: 1,
  largeItemCount: 0,
  appointmentTime: null,
  contactName: '张三',
  contactPhone: '13800000000'
})
const step = computed(() => form.vehicleTypeId && form.startAddress && form.endAddress ? 2 : form.vehicleTypeId ? 1 : 0)
function scrollToForm(){ formRef.value?.scrollIntoView({behavior:'smooth'}) }
function selectVehicle(v){ form.vehicleTypeId=v.id; estimate() }
function pick(type,p){ if(type==='start'){form.startAddress=p.address;form.startLng=p.lng;form.startLat=p.lat}else{form.endAddress=p.address;form.endLng=p.lng;form.endLat=p.lat} estimate() }
function payload(){ return { ...form, appointmentTime: form.appointmentTime || null } }
async function estimate(){ if(!form.vehicleTypeId) return; estimating.value=true; try{ fee.value=await http.post('/common/estimate', payload()) } finally{ estimating.value=false } }
async function create(){
  submitting.value=true
  try{
    await estimate()
    const order=await http.post('/user/orders', payload())
    ElMessage.success('订单已发布')
    router.push(`/orders/${order.id}`)
  } finally {
    submitting.value=false
  }
}
watch(() => [form.startFloor,form.endFloor,form.startHasElevator,form.endHasElevator,form.largeItemCount,form.appointmentTime], estimate)
onMounted(async()=>{ loading.value=true; try{ vehicles.value=await http.get('/common/vehicle-types'); form.vehicleTypeId=vehicles.value[0]?.id; await estimate() } finally{ loading.value=false } })
</script>

<style scoped>
h2{margin-top:24px}.fee-panel{display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding:18px;border-radius:16px;background:var(--primary-soft)}
</style>
