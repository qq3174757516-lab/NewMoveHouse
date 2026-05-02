<template><el-tag :type="type" effect="light" round>{{ label }}</el-tag></template>
<script setup>
import { computed } from 'vue'
import { orderStatusCn } from '../utils/orderStatus'

const props = defineProps({ status: String })
const type = computed(() =>
  props.status === 'COMPLETED'
    ? 'success'
    : props.status === 'CANCELED'
      ? 'info'
      : ['ACCEPTED', 'ARRIVED_LOADING', 'MOVING', 'MOVED'].includes(props.status)
        ? 'warning'
        : 'primary'
)
const label = computed(() => orderStatusCn(props.status))
</script>
