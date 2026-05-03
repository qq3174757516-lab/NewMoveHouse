<template>
  <div class="login">
    <el-card class="login-card">
      <h2>搬家预约系统</h2>
      <p class="muted">请选择登录入口</p>
      <el-segmented v-model="roleEntry" :options="roleOptions" class="role-switch" @change="switchRole" />
      <el-tabs v-model="mode">
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>
      <el-form ref="formRef" :model="form" :rules="activeRules" label-width="80px" autocomplete="off">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" autocomplete="off" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" autocomplete="new-password" /></el-form-item>
        <template v-if="mode==='register'">
          <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" maxlength="11" autocomplete="off" /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="form.nickname" autocomplete="off" /></el-form-item>
        </template>
        <el-button type="primary" class="full" :loading="loading" @click="submit">
          {{ mode==='login' ? '用户登录' : '用户注册' }}
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const mode = ref('login')
const roleEntry = ref('USER')
const loading = ref(false)
const formRef = ref(null)
const roleOptions = [
  { label: '用户登录', value: 'USER' },
  { label: '司机登录', value: 'DRIVER' },
  { label: '管理员登录', value: 'ADMIN' }
]
const roleUrls = {
  USER: 'http://127.0.0.1:5173/login',
  DRIVER: 'http://127.0.0.1:5174/login',
  ADMIN: 'http://127.0.0.1:5175/login'
}
const form = reactive({ username: '', password: '', phone: '', nickname: '' })

const phonePattern = /^1[3-9]\d{9}$/

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  ...loginRules,
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phonePattern, message: '请输入11位中国大陆手机号（1开头）', trigger: 'blur' }
  ]
}

const activeRules = computed(() => (mode.value === 'register' ? registerRules : loginRules))

function switchRole(role) {
  if (role !== 'USER') window.location.href = roleUrls[role]
}

async function submit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const data = mode.value === 'login'
      ? await http.post('/auth/login', { username: form.username, password: form.password, role: 'USER' })
      : await http.post('/auth/user/register', form)
    auth.setAuth(data)
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login{min-height:100vh;display:grid;place-items:center;background:#eef4fb}.login-card{width:430px;border-radius:16px}.full{width:100%;margin-top:8px}.role-switch{width:100%;margin:12px 0 10px}
</style>
