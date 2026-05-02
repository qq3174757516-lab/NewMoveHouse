<template>
  <div class="page">
    <div class="toolbar" style="justify-content:space-between">
      <h1>车辆类型管理</h1>
      <div><el-button @click="load">刷新</el-button><el-button type="primary" @click="openCreate">新增车型</el-button></div>
    </div>
    <div class="card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索车型名称" style="width:240px" />
        <el-button @click="load">搜索</el-button>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="filtered" border>
          <el-table-column prop="name" label="车型名称" min-width="140"/>
          <el-table-column prop="basePrice" label="起步价" width="110"/>
          <el-table-column prop="perKmPrice" label="每公里价格" width="120"/>
          <el-table-column prop="loadCapacity" label="最大载重" width="110"/>
          <el-table-column prop="sortOrder" label="排序号" width="100"/>
          <el-table-column label="状态" width="100"><template #default="{row}"><el-tag :type="row.enabled?'success':'info'">{{row.enabled?'启用':'禁用'}}</el-tag></template></el-table-column>
          <el-table-column prop="description" label="描述" min-width="180"/>
          <el-table-column label="操作" width="280" fixed="right"><template #default="{row}"><el-button @click="openEdit(row)">编辑</el-button><el-button :type="row.enabled?'warning':'success'" @click="toggle(row)">{{row.enabled?'禁用':'启用'}}</el-button><el-button type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
        </el-table>
      </div>
    </div>
    <el-dialog v-model="visible" :title="form.id?'编辑车型':'新增车型'" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="车型名称"><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="起步价"><el-input-number v-model="form.basePrice" :min="0"/></el-form-item>
        <el-form-item label="每公里价格"><el-input-number v-model="form.perKmPrice" :min="0"/></el-form-item>
        <el-form-item label="最大载重"><el-input-number v-model="form.loadCapacity" :min="0"/></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="form.sortOrder" :min="0"/></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.enabled" :active-value="1" :inactive-value="0"/></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
const rows=ref([]); const loading=ref(false); const saving=ref(false); const visible=ref(false); const keyword=ref('')
const form=reactive({id:null,name:'',description:'',basePrice:88,perKmPrice:4,loadCapacity:1,sortOrder:0,enabled:1})
const filtered=computed(()=>rows.value.filter(r=>!keyword.value||r.name.includes(keyword.value)))
function reset(){Object.assign(form,{id:null,name:'',description:'',basePrice:88,perKmPrice:4,loadCapacity:1,sortOrder:0,enabled:1})}
function openCreate(){reset();visible.value=true}
function openEdit(row){Object.assign(form,row);visible.value=true}
async function load(){loading.value=true;try{rows.value=await http.get('/admin/vehicle-types')}finally{loading.value=false}}
async function save(){saving.value=true;try{await http.post('/admin/vehicle-types',form);ElMessage.success('保存成功');visible.value=false;load()}finally{saving.value=false}}
async function toggle(row){await http.post('/admin/vehicle-types',{...row,enabled:row.enabled?0:1});ElMessage.success('状态已更新');load()}
async function remove(row){await ElMessageBox.confirm(`确认删除车型「${row.name}」？`,'删除确认',{type:'warning'});await http.delete(`/admin/vehicle-types/${row.id}`);ElMessage.success('删除成功');load()}
onMounted(load)
</script>
