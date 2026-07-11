export interface AuthResponse {
  token: string
  email: string
  name: string
  role: string
}

export interface LoginRequest {
  email: string
  password: string
}
