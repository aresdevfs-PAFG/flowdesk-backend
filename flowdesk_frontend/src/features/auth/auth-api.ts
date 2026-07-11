import { api } from '../../lib/api'
import type { AuthResponse, LoginRequest, RegisterRequest } from '../../types/auth'

export async function login(payload: LoginRequest) {
  const { data } = await api.post<AuthResponse>('/auth/login', payload)
  return data
}

export async function register(payload: RegisterRequest) {
  const { data } = await api.post<AuthResponse>('/auth/register', payload)
  return data
}
