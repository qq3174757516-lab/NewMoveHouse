<template><el-tag :type="type" effect="light" round>{{ label }}</el-tag></template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ status: String })
const labelMap = { WAITING_ACCEPT:'待接单', ACCEPTED:'已接单', ARRIVED_LOADING:'已到达', MOVING:'搬运中', MOVED:'待支付', COMPLETED:'已完成', CANCELED:'已取消' }
const type = computed(() => props.status === 'COMPLETED' ? 'success' : props.status === 'CANCELED' ? 'info' : ['ACCEPTED','ARRIVED_LOADING','MOVING','MOVED'].includes(props.status) ? 'warning' : 'primary')
const label = computed(() => labelMap[props.status] || props.status)
</script>
