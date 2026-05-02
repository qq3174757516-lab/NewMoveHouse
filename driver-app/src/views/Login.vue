<template>
  <div class="login">
    <el-card class="login-card">
      <h2>搬家预约系统</h2>
      <p class="muted">请选择登录入口</p>
      <el-segmented v-model="roleEntry" :options="roleOptions" class="role-switch" @change="switchRole" />
      <el-tabs v-model="mode">
        <el-tab-pane label="登录" name="login"/>
        <el-tab-pane label="注册" name="register"/>
      </el-tabs>
      <el-form :model="form" label-width="90px" autocomplete="off">
        <el-form-item label="用户名"><el-input v-model="form.username" autocomplete="off"/></el-form-item>
        <el-form-item label="密码"><el-input type="password" v-model="form.password" autocomplete="new-password"/></el-form-item>
        <template v-if="mode==='register'">
          <el-form-item label="姓名"><el-input v-model="form.realName" autocomplete="off"/></el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" autocomplete="off"/></el-form-item>
          <el-form-item label="车牌"><el-input v-model="form.vehiclePlate" autocomplete="off"/></el-form-item>
          <el-form-item label="车型"><el-select v-model="form.vehicleTypeId"><el-option v-for="v in vehicles" :key="v.id" :label="v.name" :value="v.id"/></el-select></el-form-item>
          <el-form-item label="服务区域"><el-input v-model="form.serviceArea" autocomplete="off"/></el-form-item>
        </template>
        <el-button type="primary" class="full" :loading="loading" @click="submit">{{mode==='login'?'司机登录':'司机注册'}}</el-button>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'
const router=useRouter();const auth=useAuthStore();const mode=ref('login');const vehicles=ref([]);const loading=ref(false);const roleEntry=ref('DRIVER')
const roleOptions=[{label:'用户登录',value:'USER'},{label:'司机登录',value:'DRIVER'},{label:'管理员登录',value:'ADMIN'}]
const roleUrls={USER:'http://127.0.0.1:5173/login',DRIVER:'http://127.0.0.1:5174/login',ADMIN:'http://127.0.0.1:5175/login'}
const form=reactive({username:'',password:'',phone:'',realName:'',vehiclePlate:'',vehicleTypeId:null,serviceArea:''})
function switchRole(role){ if(role!=='DRIVER') window.location.href=roleUrls[role] }
async function submit(){
  loading.value=true
  try{
    if(mode.value==='register' && !form.vehicleTypeId){
      form.vehicleTypeId = vehicles.value?.[0]?.id || null
    }
    const data=mode.value==='login'
      ? await http.post('/auth/login',{username:form.username,password:form.password,role:'DRIVER'})
      : await http.post('/auth/driver/register',form)
    auth.setAuth(data)
    router.push('/')
  }finally{
    loading.value=false
  }
}
onMounted(async()=>{
  vehicles.value=await http.get('/common/vehicle-types')
})
</script>
<style scoped>.login{min-height:100vh;display:grid;place-items:center;background:#eef4fb}.login-card{width:450px;border-radius:16px}.full{width:100%}.role-switch{width:100%;margin:12px 0 10px}</style>
