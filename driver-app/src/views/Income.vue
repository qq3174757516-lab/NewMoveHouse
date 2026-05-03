<template>
  <div class="page">
    <div class="toolbar" style="justify-content:space-between">
      <h1>收入统计</h1>
      <el-button @click="load">刷新</el-button>
    </div>

    <div class="dash-stats">
      <div class="stat-card"><span class="lbl">累计收入</span><b class="num">￥{{ fmt(d.totalIncome) }}</b></div>
      <div class="stat-card"><span class="lbl">今日收入</span><b class="num">￥{{ fmt(d.todayIncome) }}</b></div>
      <div class="stat-card"><span class="lbl">本月收入</span><b class="num">￥{{ fmt(d.monthIncome) }}</b></div>
      <div class="stat-card"><span class="lbl">总订单</span><b class="num">{{ d.totalOrders ?? 0 }}</b></div>
      <div class="stat-card"><span class="lbl">已完成</span><b class="num">{{ d.completedOrders ?? 0 }}</b></div>
      <div class="stat-card"><span class="lbl">进行中</span><b class="num">{{ d.runningOrders ?? 0 }}</b></div>
      <div class="stat-card"><span class="lbl">待处理/处理中投诉</span><b class="num">{{ d.complaintTicketOpenCount ?? 0 }}</b></div>
      <div class="stat-card"><span class="lbl">累计投诉工单</span><b class="num">{{ d.complaintTicketTotalCount ?? 0 }}</b></div>
      <div class="stat-card"><span class="lbl">低分评价(≤2星)</span><b class="num">{{ d.lowRatingComplaintCount ?? 0 }}</b></div>
      <div class="stat-card muted-card"><span class="lbl">历史口径参考</span><span class="sub">完成 {{ legacy.completedCount ?? 0 }} 单 · 今日完成 {{ legacy.todayCount ?? 0 }} 单</span></div>
    </div>

    <div class="card" style="margin-top:14px">
      <div class="toolbar" style="justify-content:space-between;margin:0 0 10px">
        <h2>近7天完成趋势</h2>
        <span class="muted">按完成日期倒序，最新在上</span>
      </div>
      <el-table :data="trendRows" border>
        <el-table-column prop="day" label="日期" width="140" />
        <el-table-column prop="cnt" label="完成单量" width="120" />
        <el-table-column label="收入">
          <template #default="{ row }">￥{{ fmt(row.income) }}</template>
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

function fmt(v) {
  return Number(v || 0).toFixed(2)
}

/** 从今天往前共7天，日期倒序（最新在上） */
const trendRows = computed(() => {
  const m = {}
  for (const x of trend.value || []) {
    m[String(x.day)] = x
  }
  const rows = []
  for (let i = 0; i <= 6; i++) {
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

<style scoped>
.dash-stats {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}
.stat-card {
  border-radius: 14px;
  padding: 14px 16px;
  background: linear-gradient(145deg, #ffffff 0%, #f3f6fb 100%);
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
}
.stat-card .lbl {
  display: block;
  font-size: 13px;
  color: #64748b;
}
.stat-card .num {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}
.stat-card.muted-card .sub {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}
</style>
