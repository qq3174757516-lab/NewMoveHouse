/** 订单/支付/投诉状态中文展示（后端仍存英文枚举） */
export const ORDER_STATUS_CN = {
  WAITING_ACCEPT: '待接单',
  ACCEPTED: '已接单',
  ARRIVED_LOADING: '到达装货地',
  MOVING: '搬运中',
  MOVED: '待支付',
  COMPLETED: '订单完成',
  CANCELED: '已取消'
}

export const PAYMENT_STATUS_CN = {
  UNPAID: '未支付',
  PAID: '已支付'
}

export const COMPLAINT_STATUS_CN = {
  OPEN: '待处理',
  IN_PROGRESS: '处理中',
  RESOLVED: '已解决',
  CLOSED: '已关闭'
}

export const AUDIT_STATUS_CN = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  DISABLED: '已停用'
}

export const USER_ACCOUNT_STATUS_CN = {
  0: '已禁用',
  1: '正常'
}

export function orderStatusCn(code) {
  if (code == null || code === '') return ''
  return ORDER_STATUS_CN[code] || String(code)
}

export function paymentStatusCn(code) {
  if (code == null || code === '') return ''
  return PAYMENT_STATUS_CN[code] || String(code)
}

export function complaintStatusCn(code) {
  if (code == null || code === '') return ''
  return COMPLAINT_STATUS_CN[code] || String(code)
}

export function auditStatusCn(code) {
  if (code == null || code === '') return ''
  return AUDIT_STATUS_CN[code] || String(code)
}

export function userAccountStatusCn(status) {
  if (status === undefined || status === null) return ''
  const k = Number(status)
  return USER_ACCOUNT_STATUS_CN[k] || String(status)
}

/** 评价是否隐藏：1/0 → 中文 */
export function reviewHiddenCn(hidden) {
  const v = Number(hidden)
  if (v === 1) return '已隐藏'
  if (v === 0) return '正常展示'
  return hidden == null ? '' : String(hidden)
}
