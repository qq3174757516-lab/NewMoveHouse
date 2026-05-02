<template>
  <div class="page">
    <h1>车型与计价</h1>
    <div class="card">
      <h3>车型</h3>
      <el-form :inline="true" :model="vehicle">
        <el-form-item label="名称"><el-input v-model="vehicle.name"/></el-form-item>
        <el-form-item label="起步价"><el-input-number v-model="vehicle.basePrice"/></el-form-item>
        <el-form-item label="公里价"><el-input-number v-model="vehicle.perKmPrice"/></el-form-item>
        <el-form-item label="载重"><el-input-number v-model="vehicle.loadCapacity"/></el-form-item>
        <el-button @click="saveVehicle">保存</el-button>
      </el-form>
      <el-table :data="vehicles" @row-click="r=>Object.assign(vehicle,r)">
        <el-table-column prop="name" label="名称"/>
        <el-table-column prop="basePrice" label="起步价"/>
        <el-table-column prop="perKmPrice" label="公里价"/>
      </el-table>
    </div>
    <div class="card">
      <h3>计价规则</h3>
      <el-table :data="rules" border>
        <el-table-column label="规则">
          <template #default="{row}">{{ keyCn(row.ruleKey) }}</template>
        </el-table-column>
        <el-table-column prop="ruleValue" label="值">
          <template #default="{row}"><el-input-number v-model="row.ruleValue"/></template>
        </el-table-column>
        <el-table-column prop="description" label="说明"/>
        <el-table-column label="操作">
          <template #default="{row}"><el-button @click="saveRule(row)">保存</el-button></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../api/http'
const vehicles=ref([]);const rules=ref([]);const vehicle=reactive({name:'',description:'',basePrice:100,perKmPrice:5,loadCapacity:1,enabled:1})
const ruleKeyMap = {
  floor_fee_per_floor: '无电梯楼层费（每层）',
  large_item_fee: '大件附加费（每件）',
  night_service_fee: '夜间服务费'
}
const keyCn = k => ruleKeyMap[k] || k
async function load(){vehicles.value=await http.get('/admin/vehicle-types');rules.value=await http.get('/admin/pricing-rules')}
async function saveVehicle(){await http.post('/admin/vehicle-types',vehicle);Object.assign(vehicle,{id:null,name:'',description:'',basePrice:100,perKmPrice:5,loadCapacity:1,enabled:1});load()}
async function saveRule(r){await http.post('/admin/pricing-rules',r);load()}
onMounted(load)
</script>
