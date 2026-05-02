import AMapLoader from '@amap/amap-jsapi-loader'

/** 与后端代理一致的前缀，例如 http://localhost:8080/api 或 /api */
export function apiBase() {
  return (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')
}

/**
 * 解析高德 JSAPI Key 与安全密钥：优先读 Vite 环境变量，缺失时请求后端 `/api/common/map-web-config`。
 * 对应高德 JSAPI 2.0 安全模式（需在加载前设置 window._AMapSecurityConfig）。
 */
export async function resolveAmapKeys() {
  let key = import.meta.env.VITE_AMAP_WEB_KEY
  let securityJsCode = import.meta.env.VITE_AMAP_SECURITY_JS_CODE
  if (key && !String(key).startsWith('your')) {
    return { key, securityJsCode: securityJsCode || '' }
  }
  const res = await fetch(`${apiBase()}/common/map-web-config`)
  const json = await res.json()
  if (json && json.code === 0 && json.data && json.data.key) {
    return {
      key: json.data.key,
      securityJsCode: json.data.securityJsCode || ''
    }
  }
  throw new Error('未配置高德地图 Web Key（请在 user-app/.env 设置 VITE_AMAP_WEB_KEY，或在后端 application.yml 配置 amap.web-key）')
}

/** 加载高德 JSAPI，并自动写入安全密钥配置 */
export async function loadAmap(plugins) {
  const { key, securityJsCode } = await resolveAmapKeys()
  if (securityJsCode) {
    window._AMapSecurityConfig = { securityJsCode }
  }
  return AMapLoader.load({ key, version: '2.0', plugins: plugins || [] })
}
