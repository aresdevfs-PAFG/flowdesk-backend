import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { AuthResponse } from '../types/auth'

interface AuthState {
  session: AuthResponse | null
  setSession: (session: AuthResponse) => void
  clearSession: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      session: null,
      setSession: (session) => set({ session }),
      clearSession: () => set({ session: null }),
    }),
    { name: 'flowdesk-session' },
  ),
)
