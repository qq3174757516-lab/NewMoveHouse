<template>
  <div class="login">
    <el-card class="login-card">
      <h2>搬家预约系统</h2>
      <p class="muted">请选择登录入口</p>
      <el-segmented v-model="roleEntry" :options="roleOptions" class="role-switch" @change="switchRole" />
      <el-form :model="form" label-width="80px" autocomplete="off">
        <el-form-item label="用户名"><el-input v-model="form.username" autocomplete="off"/></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" autocomplete="new-password"/></el-form-item>
        <el-button type="primary" class="full" :loading="loading" @click="submit">管理员登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'
const router=useRouter();const auth=useAuthStore();const loading=ref(false);const roleEntry=ref('ADMIN')
const roleOptions=[{label:'用户登录',value:'USER'},{label:'司机登录',value:'DRIVER'},{label:'管理员登录',value:'ADMIN'}]
const roleUrls={USER:'http://127.0.0.1:5173/login',DRIVER:'http://127.0.0.1:5174/login',ADMIN:'http://127.0.0.1:5175/login'}
const form=reactive({username:'',password:''})
function switchRole(role){ if(role!=='ADMIN') window.location.href=roleUrls[role] }
async function submit(){loading.value=true;try{const data=await http.post('/auth/login',{...form,role:'ADMIN'});auth.setAuth(data);router.push('/')}finally{loading.value=false}}
</script>
<style scoped>.login{min-height:100vh;display:grid;place-items:center;background:#eef4fb}.login-card{width:430px;border-radius:16px}.full{width:100%}.role-switch{width:100%;margin:12px 0 18px}</style>
