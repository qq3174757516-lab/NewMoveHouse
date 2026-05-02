<template>
  <div class="page">
    <h1>个人中心</h1>
    <div class="grid"><div class="card"><el-descriptions :column="1" border><el-descriptions-item label="用户名">{{ me?.username }}</el-descriptions-item><el-descriptions-item label="电话">{{ me?.phone }}</el-descriptions-item><el-descriptions-item label="昵称">{{ me?.nickname }}</el-descriptions-item></el-descriptions></div>
    <div class="card"><h3>常用地址</h3><el-form :model="addr" label-width="80px"><el-form-item label="名称"><el-input v-model="addr.name"/></el-form-item><el-form-item label="详情"><el-input v-model="addr.detail"/></el-form-item><el-form-item label="坐标"><el-input v-model="addrText"/></el-form-item><el-button @click="save">保存地址</el-button></el-form><el-table :data="addresses"><el-table-column prop="name" label="名称"/><el-table-column prop="detail" label="地址"/></el-table></div></div>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../api/http'
const me=ref(null); const addresses=ref([]); const addrText=ref('112.938882,28.228304'); const addr=reactive({name:'家',detail:'常用地址',lng:112.938882,lat:28.228304})
async function load(){ me.value=await http.get('/user/me'); addresses.value=await http.get('/user/addresses') }
async function save(){ const [lng,lat]=addrText.value.split(',').map(Number); await http.post('/user/addresses',{...addr,lng,lat}); await load() }
onMounted(load)
</script>
