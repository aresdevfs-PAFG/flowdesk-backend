import { api } from '../../lib/api'
import type { AuthResponse, LoginRequest } from '../../types/auth'

export async function login(payload: LoginRequest) {
  const { data } = await api.post<AuthResponse>('/auth/login', payload)
  return data
}
