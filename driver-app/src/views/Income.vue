<template>
  <div class="page">
    <div class="toolbar" style="justify-content:space-between">
      <h1>收入统计</h1>
      <el-button @click="load">刷新</el-button>
    </div>

    <div class="grid" style="grid-template-columns:repeat(4,minmax(0,1fr));gap:12px">
      <div class="card">
        <div class="muted">累计收入</div>
        <div style="font-size:28px;font-weight:800;margin-top:6px">￥{{ Number(d.totalIncome||0).toFixed(2) }}</div>
      </div>
      <div class="card">
        <div class="muted">今日收入</div>
        <div style="font-size:28px;font-weight:800;margin-top:6px">￥{{ Number(d.todayIncome||0).toFixed(2) }}</div>
      </div>
      <div class="card">
        <div class="muted">本月收入</div>
        <div style="font-size:28px;font-weight:800;margin-top:6px">￥{{ Number(d.monthIncome||0).toFixed(2) }}</div>
      </div>
      <div class="card">
        <div class="muted">投诉工单（待处理/处理中）</div>
        <div style="font-size:28px;font-weight:800;margin-top:6px">{{ d.complaintTicketOpenCount || 0 }}</div>
        <div class="muted" style="margin-top:8px;line-height:1.4">累计工单：{{ d.complaintTicketTotalCount || 0 }}</div>
      </div>
    </div>

    <div class="grid" style="grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;margin-top:12px">
      <div class="card">
        <div class="muted">低分评价（≤2星，代理指标）</div>
        <div style="font-size:22px;font-weight:800;margin-top:6px">{{ d.lowRatingComplaintCount || 0 }}</div>
      </div>
    </div>

    <div class="grid" style="grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;margin-top:12px">
      <div class="card">
        <div class="muted">总订单（含进行中）</div>
        <div style="font-size:22px;font-weight:800;margin-top:6px">{{ d.totalOrders || 0 }}</div>
      </div>
      <div class="card">
        <div class="muted">已完成订单</div>
        <div style="font-size:22px;font-weight:800;margin-top:6px">{{ d.completedOrders || 0 }}</div>
      </div>
      <div class="card">
        <div class="muted">进行中订单</div>
        <div style="font-size:22px;font-weight:800;margin-top:6px">{{ d.runningOrders || 0 }}</div>
      </div>
      <div class="card">
        <div class="muted">兼容：历史统计口径</div>
        <div class="muted" style="margin-top:8px;line-height:1.4">完成单量 {{ legacy.completedCount||0 }} · 今日完成 {{ legacy.todayCount||0 }}</div>
      </div>
    </div>

    <div class="card" style="margin-top:12px">
      <div class="toolbar" style="justify-content:space-between;margin:0 0 10px">
        <h2>近7天完成趋势</h2>
        <span class="muted">按完成时间统计</span>
      </div>
      <el-table :data="trendRows" border>
        <el-table-column prop="day" label="日期" width="140" />
        <el-table-column prop="cnt" label="完成单量" width="120" />
        <el-table-column label="收入">
          <template #default="{ row }">￥{{ Number(row.income||0).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../api/http'

const d = ref({})
const legacy = ref({})
const trend = ref([])

const trendRows = computed(() => {
  const m = {}
  for (const x of trend.value || []) {
    m[String(x.day)] = x
  }
  const rows = []
  for (let i = 6; i >= 0; i--) {
    const day = new Date()
    day.setDate(day.getDate() - i)
    const key = `${day.getFullYear()}-${String(day.getMonth() + 1).padStart(2, '0')}-${String(day.getDate()).padStart(2, '0')}`
    const hit = m[key]
    rows.push({ day: key, cnt: hit?.cnt || 0, income: hit?.income || 0 })
  }
  return rows
})

async function load() {
  const dash = await http.get('/driver/income/dashboard')
  d.value = dash || {}
  trend.value = dash?.trend7d || []
  legacy.value = await http.get('/driver/income')
}

onMounted(load)
</script>
