<template>
  <div class="page">
    <div class="toolbar" style="justify-content:space-between">
      <h1>系统公告</h1>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-skeleton :loading="loading" animated :rows="6">
      <el-empty v-if="rows.length===0" description="暂无公告" />
      <el-timeline v-else>
        <el-timeline-item v-for="a in rows" :key="a.id" :timestamp="a.createdAt" placement="top">
          <div class="card" style="padding:14px">
            <div class="toolbar" style="justify-content:space-between;margin:0">
              <b>{{ a.title }}</b>
              <el-tag type="info" round>用户端</el-tag>
            </div>
            <p style="white-space:pre-wrap;margin-top:10px">{{ a.content }}</p>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-skeleton>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'

const rows = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    rows.value = await http.get('/common/announcements', { params: { audience: 'USER' } })
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
