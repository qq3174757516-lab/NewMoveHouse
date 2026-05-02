import { ElNotification } from 'element-plus'

export function connectWs(token, onMessage) {
  if (!token) return null
  const base = import.meta.env.VITE_WS_URL || `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws`
  const ws = new WebSocket(`${base}?token=${encodeURIComponent(token)}`)
  ws.onmessage = event => {
    const msg = JSON.parse(event.data)
    if (msg.type === 'ORDER_STATUS_CHANGED') ElNotification.success({ title: '订单状态更新', message: msg.data.status })
    if (msg.type === 'DRIVER_AUDIT_RESULT') ElNotification.info({ title: '审核通知', message: msg.data.auditStatus })
    onMessage?.(msg)
  }
  return ws
}
