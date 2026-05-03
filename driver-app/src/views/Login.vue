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
      <el-form ref="formRef" :model="form" :rules="activeRules" label-width="90px" autocomplete="off">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" autocomplete="off"/></el-form-item>
        <el-form-item label="密码" prop="password"><el-input type="password" v-model="form.password" autocomplete="new-password"/></el-form-item>
        <template v-if="mode==='register'">
          <el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" autocomplete="off"/></el-form-item>
          <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" maxlength="11" autocomplete="off"/></el-form-item>
          <el-form-item label="车牌" prop="vehiclePlate"><el-input v-model="form.vehiclePlate" placeholder="如 湘A12345" autocomplete="off"/></el-form-item>
          <el-form-item label="车型" prop="vehicleTypeId"><el-select v-model="form.vehicleTypeId" style="width:100%"><el-option v-for="v in vehicles" :key="v.id" :label="v.name" :value="v.id"/></el-select></el-form-item>
        </template>
        <el-button type="primary" class="full" :loading="loading" @click="submit">{{mode==='login'?'司机登录':'司机注册'}}</el-button>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const mode = ref('login')
const vehicles = ref([])
const loading = ref(false)
const roleEntry = ref('DRIVER')
const formRef = ref(null)
const roleOptions = [{label:'用户登录',value:'USER'},{label:'司机登录',value:'DRIVER'},{label:'管理员登录',value:'ADMIN'}]
const roleUrls = {USER:'http://127.0.0.1:5173/login',DRIVER:'http://127.0.0.1:5174/login',ADMIN:'http://127.0.0.1:5175/login'}

const form = reactive({username:'',password:'',phone:'',realName:'',vehiclePlate:'',vehicleTypeId:null})

const phonePattern = /^1[3-9]\d{9}$/
const platePattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵青藏川宁琼][A-HJ-NP-Z][A-HJ-NP-Z0-9]{4,6}[A-HJ-NP-Z0-9挂学警港澳]?$/

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  ...loginRules,
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phonePattern, message: '请输入11位中国大陆手机号', trigger: 'blur' }
  ],
  vehiclePlate: [
    { required: true, message: '请输入车牌号', trigger: 'blur' },
    { pattern: platePattern, message: '车牌格式不正确（省简称+字母+号码）', trigger: 'blur' }
  ],
  vehicleTypeId: [{ required: true, message: '请选择车型', trigger: 'change' }]
}

const activeRules = computed(() => (mode.value === 'register' ? registerRules : loginRules))

function switchRole(role){ if(role!=='DRIVER') window.location.href=roleUrls[role] }

async function submit(){
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value=true
  try{
    const data=mode.value==='login'
      ? await http.post('/auth/login',{username:form.username,password:form.password,role:'DRIVER'})
      : await http.post('/auth/driver/register',{
          username: form.username,
          password: form.password,
          phone: form.phone,
          realName: form.realName,
          vehiclePlate: form.vehiclePlate,
          vehicleTypeId: form.vehicleTypeId
        })
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
<style scoped>.login{min-height:100vh;display:grid;place-items:center;background:#eef4fb}.login-card{width:450px;border-radius:16px}.full{width:100%;margin-top:8px}.role-switch{width:100%;margin:12px 0 10px}</style>
