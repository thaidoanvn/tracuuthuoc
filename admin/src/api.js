const TOKEN_KEY = 'adminToken'
const PRODUCTION_API = 'https://tracuuthuoc-production.up.railway.app'
const API_BASE = import.meta.env.VITE_API_BASE || (import.meta.env.PROD ? PRODUCTION_API : '')

export function apiUrl(path) {
  return `${API_BASE}${path}`
}

export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) {
    sessionStorage.setItem(TOKEN_KEY, token)
  } else {
    sessionStorage.removeItem(TOKEN_KEY)
  }
}

export async function api(path, options = {}) {
  const headers = { ...(options.headers || {}) }
  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  const response = await fetch(apiUrl(path), { ...options, headers })
  const text = await response.text()
  const data = text ? JSON.parse(text) : null
  if (!response.ok) {
    const error = new Error(data?.message || 'Yêu cầu không thành công')
    error.status = response.status
    throw error
  }
  return data
}
