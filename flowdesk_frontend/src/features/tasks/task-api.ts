import { api } from '../../lib/api'
import type { Task, TaskPriority, TaskStatus } from '../../types/task'

export interface CreateTaskRequest {
  title: string
  description: string | null
  priority: TaskPriority
  assigneeId: string | null
  dueDate: string | null
}

export async function getTasks(projectId: string) {
  const { data } = await api.get<Task[]>(`/projects/${projectId}/tasks`)
  return data
}

export async function createTask(projectId: string, payload: CreateTaskRequest) {
  const { data } = await api.post<Task>(`/projects/${projectId}/tasks`, payload)
  return data
}

export async function updateTaskStatus(projectId: string, taskId: string, status: TaskStatus) {
  const { data } = await api.patch<Task>(`/projects/${projectId}/tasks/${taskId}/status`, { status })
  return data
}
