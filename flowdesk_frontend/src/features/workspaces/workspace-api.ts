import { api } from '../../lib/api'
import type { Workspace } from '../../types/workspace'

export interface CreateWorkspaceRequest {
  name: string
  slug: string
}

export async function getWorkspaces() {
  const { data } = await api.get<Workspace[]>('/workspaces')
  return data
}

export async function getWorkspace(workspaceId: string) {
  const { data } = await api.get<Workspace>(`/workspaces/${workspaceId}`)
  return data
}

export async function createWorkspace(payload: CreateWorkspaceRequest) {
  const { data } = await api.post<Workspace>('/workspaces', payload)
  return data
}
