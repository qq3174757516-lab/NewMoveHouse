<template>
  <div class="page">
    <h1>数据看板</h1>
    <div class="stats">
      <div class="stat"><span class="muted">总订单数</span><b>{{ d.totalOrders || 0 }}</b><p class="muted">全部历史订单</p></div>
      <div class="stat"><span class="muted">今日新增</span><b>{{ d.todayOrders || 0 }}</b><p class="muted">按创建时间统计</p></div>
      <div class="stat"><span class="muted">活跃司机</span><b>{{ d.driverCount || 0 }}</b><p class="muted">注册司机总数</p></div>
      <div class="stat"><span class="muted">用户数</span><b>{{ d.userCount || 0 }}</b><p class="muted">注册用户总数</p></div>
    </div>
    <div class="card">
      <div class="toolbar" style="justify-content:space-between">
        <h2>近7天订单趋势</h2>
        <span class="muted">按订单创建时间</span>
      </div>
      <div ref="chartRef" style="height:280px"></div>
    </div>
    <div class="card">
      <div class="toolbar"><h2>最近订单</h2><el-button @click="load">刷新</el-button></div>
      <el-table :data="orders" border>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="userName" label="用户"/>
        <el-table-column prop="driverName" label="司机"/>
        <el-table-column prop="startAddress" label="起点"/>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">{{ orderStatusCn(row.status) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import http from '../api/http'
import { orderStatusCn } from '../utils/orderStatus'

const d = ref({})
const orders = ref([])
const chartRef = ref(null)

function pad(n) {
  return String(n).padStart(2, '0')
}

/** 近7天日期标签 MM-DD */
function last7DaysLabels() {
  const labels = []
  for (let i = 6; i >= 0; i--) {
    const dt = new Date()
    dt.setDate(dt.getDate() - i)
    labels.push(`${pad(dt.getMonth() + 1)}-${pad(dt.getDate())}`)
  }
  return labels
}

/** yyyy-MM-dd */
function dayKey(offsetFromToday) {
  const dt = new Date()
  dt.setDate(dt.getDate() - offsetFromToday)
  return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(dt.getDate())}`
}

function buildSeriesFromTrend(trend) {
  const map = {}
  for (const row of trend || []) {
    map[String(row.day)] = Number(row.cnt || 0)
  }
  const data = []
  for (let i = 6; i >= 0; i--) {
    data.push(map[dayKey(i)] ?? 0)
  }
  return data
}

async function load() {
  d.value = await http.get('/admin/dashboard')
  orders.value = (await http.get('/admin/orders')).slice(0, 6)
  await nextTick()
  renderChart()
}

function renderChart() {
  const chart = echarts.init(chartRef.value)
  const trend = d.value.ordersTrend7d || []
  const xLabels = last7DaysLabels()
  const seriesData = buildSeriesFromTrend(trend)
  chart.setOption({
    color: ['#ff6b35'],
    tooltip: { trigger: 'axis' },
    grid: { left: 44, right: 18, top: 30, bottom: 36 },
    xAxis: {
      type: 'category',
      data: xLabels,
      axisLabel: { color: '#4b5563' }
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'line',
      smooth: true,
      areaStyle: { color: 'rgba(255,107,53,.12)' },
      data: seriesData
    }]
  })
}

onMounted(load)
</script>
