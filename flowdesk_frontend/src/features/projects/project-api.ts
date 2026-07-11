import { api } from '../../lib/api'
import type { Project } from '../../types/project'

export interface CreateProjectRequest {
  name: string
  description: string | null
  hourlyRate: number
  startDate: string | null
  endDate: string | null
}

export async function getProjects(workspaceId: string) {
  const { data } = await api.get<Project[]>(`/workspaces/${workspaceId}/projects`)
  return data
}

export async function getProject(workspaceId: string, projectId: string) {
  const { data } = await api.get<Project>(`/workspaces/${workspaceId}/projects/${projectId}`)
  return data
}

export async function createProject(workspaceId: string, payload: CreateProjectRequest) {
  const { data } = await api.post<Project>(`/workspaces/${workspaceId}/projects`, payload)
  return data
}
