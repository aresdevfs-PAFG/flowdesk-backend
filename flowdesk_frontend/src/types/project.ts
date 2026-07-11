export type ProjectStatus = 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED'

export interface Project {
  id: string
  workspaceId: string
  name: string
  description: string | null
  status: ProjectStatus
  hourlyRate: number
  startDate: string | null
  endDate: string | null
  memberCount: number
  taskCount: number
  createdAt: string
}
