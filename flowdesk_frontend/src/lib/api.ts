import axios from 'axios'
import { useAuthStore } from '../stores/auth-store'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().session?.token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().clearSession()
      if (window.location.pathname !== '/login') window.location.assign('/login')
    }
    return Promise.reject(error)
  },
)

export function getApiError(error: unknown) {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.error ?? 'No pudimos comunicarnos con FlowDesk.'
  }
  return 'Ocurrió un error inesperado.'
}
