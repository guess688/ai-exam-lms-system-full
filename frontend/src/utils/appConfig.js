function getRuntimeConfig() {
  if (typeof window === 'undefined' || !window.__APP_CONFIG__) {
    return {}
  }
  return window.__APP_CONFIG__
}

function trimTrailingSlash(value) {
  return value && value.length > 1 ? value.replace(/\/+$/, '') : value
}

export function getApiBaseUrl() {
  const runtimeApiBaseUrl = getRuntimeConfig().API_BASE_URL
  return trimTrailingSlash(runtimeApiBaseUrl || process.env.VUE_APP_BASE_API || '/api')
}

export function getWsBaseUrl() {
  return trimTrailingSlash(getRuntimeConfig().WS_BASE_URL || '/api/websocket')
}

export function joinApiPath(path) {
  const baseUrl = getApiBaseUrl()
  if (!path) {
    return baseUrl
  }
  const normalizedPath = path.charAt(0) === '/' ? path : `/${path}`
  return `${baseUrl}${normalizedPath}`
}
