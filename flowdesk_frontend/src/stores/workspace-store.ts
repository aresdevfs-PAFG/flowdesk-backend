import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface WorkspaceState {
  activeWorkspaceId: string | null
  setActiveWorkspaceId: (workspaceId: string | null) => void
}

export const useWorkspaceStore = create<WorkspaceState>()(
  persist(
    (set) => ({
      activeWorkspaceId: null,
      setActiveWorkspaceId: (activeWorkspaceId) => set({ activeWorkspaceId }),
    }),
    { name: 'flowdesk-workspace' },
  ),
)
