import { useEffect, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, CheckSquare, Clock3, GripVertical, ListTodo, Plus, UsersRound } from 'lucide-react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { getApiError } from '../../lib/api'
import { useWorkspaceStore } from '../../stores/workspace-store'
import type { Task, TaskPriority, TaskStatus } from '../../types/task'
import { TaskCreateModal } from '../tasks/TaskCreateModal'
import { TaskDetailModal } from '../tasks/TaskDetailModal'
import { getProject } from './project-api'
import { getTasks, updateTaskStatus } from '../tasks/task-api'

const columns: { status: TaskStatus; label: string; marker: string }[] = [
  { status: 'TODO', label: 'Por hacer', marker: 'bg-slate-400' },
  { status: 'IN_PROGRESS', label: 'En progreso', marker: 'bg-brand-400' },
  { status: 'IN_REVIEW', label: 'En revision', marker: 'bg-amber-400' },
  { status: 'DONE', label: 'Completado', marker: 'bg-emerald-400' },
  { status: 'CANCELLED', label: 'Cancelado', marker: 'bg-slate-600' },
]

const priorityStyle: Record<TaskPriority, string> = {
  LOW: 'text-slate-400',
  MEDIUM: 'text-brand-300',
  HIGH: 'text-amber-300',
  CRITICAL: 'text-red-300',
}

const priorityCopy: Record<TaskPriority, string> = {
  LOW: 'Baja',
  MEDIUM: 'Media',
  HIGH: 'Alta',
  CRITICAL: 'Critica',
}

export function ProjectBoardPage() {
  const { workspaceId, projectId } = useParams()
  const queryClient = useQueryClient()
  const setActiveWorkspaceId = useWorkspaceStore((state) => state.setActiveWorkspaceId)
  const [taskModalOpen, setTaskModalOpen] = useState(false)
  const [selectedTask, setSelectedTask] = useState<Task | null>(null)
  const [draggedTaskId, setDraggedTaskId] = useState<string | null>(null)
  const project = useQuery({ queryKey: ['project', workspaceId, projectId], queryFn: () => getProject(workspaceId!, projectId!), enabled: Boolean(workspaceId && projectId) })
  const tasks = useQuery({ queryKey: ['tasks', projectId], queryFn: () => getTasks(projectId!), enabled: Boolean(projectId) })

  useEffect(() => {
    if (workspaceId) setActiveWorkspaceId(workspaceId)
  }, [setActiveWorkspaceId, workspaceId])

  const updateStatus = useMutation({
    mutationFn: ({ taskId, status }: { taskId: string; status: TaskStatus }) => updateTaskStatus(projectId!, taskId, status),
    onMutate: async ({ taskId, status }) => {
      await queryClient.cancelQueries({ queryKey: ['tasks', projectId] })
      const previous = queryClient.getQueryData<Task[]>(['tasks', projectId])
      queryClient.setQueryData<Task[]>(['tasks', projectId], (current) => current?.map((task) => task.id === taskId ? { ...task, status } : task))
      return { previous }
    },
    onError: (_error, _variables, context) => {
      queryClient.setQueryData(['tasks', projectId], context?.previous)
    },
    onSuccess: (updated) => {
      queryClient.setQueryData<Task[]>(['tasks', projectId], (current) => current?.map((task) => task.id === updated.id ? updated : task))
      setSelectedTask((current) => current?.id === updated.id ? updated : current)
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['tasks', projectId] }),
  })

  const tasksByStatus = useMemo(() => {
    const grouped = new Map<TaskStatus, Task[]>(columns.map(({ status }) => [status, []]))
    tasks.data?.forEach((task) => grouped.get(task.status)?.push(task))
    return grouped
  }, [tasks.data])

  if (!workspaceId || !projectId) return <Navigate to="/" replace />

  const completedTasks = tasksByStatus.get('DONE')?.length ?? 0
  const progress = tasks.data?.length ? Math.round((completedTasks / tasks.data.length) * 100) : 0

  function moveTask(taskId: string, status: TaskStatus) {
    const task = tasks.data?.find((item) => item.id === taskId)
    if (!task || task.status === status || updateStatus.isPending) return
    updateStatus.mutate({ taskId, status })
  }

  return (
    <div className="space-y-8">
      {project.isLoading && <BoardLoading />}
      {project.isError && <BoardError message={getApiError(project.error)} />}
      {project.data && (
        <>
          <section className="flex flex-col gap-6 xl:flex-row xl:items-end xl:justify-between">
            <div className="min-w-0">
              <Link to={`/workspaces/${workspaceId}`} className="inline-flex items-center gap-2 text-xs font-bold text-slate-500 transition hover:text-brand-400"><ArrowLeft size={15} /> {project.data.workspaceId ? 'Volver al workspace' : 'Volver'}</Link>
              <div className="mt-4 flex items-start gap-4"><div className="grid h-12 w-12 shrink-0 place-items-center rounded-2xl bg-brand-500/15 text-brand-400"><CheckSquare size={23} /></div><div><p className="eyebrow">Tablero Kanban</p><h1 className="mt-2 truncate text-3xl font-semibold tracking-[-0.045em] text-white sm:text-4xl">{project.data.name}</h1><p className="mt-2 max-w-2xl text-sm text-slate-400">{project.data.description || 'Organiza el flujo de trabajo de este proyecto.'}</p></div></div>
            </div>
            <button onClick={() => setTaskModalOpen(true)} className="flex items-center justify-center gap-2 rounded-xl bg-white px-4 py-3 text-sm font-bold text-slate-950 transition hover:bg-slate-200"><Plus size={17} /> Nueva tarea</button>
          </section>

          <section className="grid gap-4 sm:grid-cols-3">
            <Metric icon={ListTodo} label="Tareas" value={tasks.data?.length ?? 0} note="En este proyecto" />
            <Metric icon={CheckSquare} label="Completadas" value={completedTasks} note={`${progress}% del proyecto`} />
            <Metric icon={UsersRound} label="Equipo" value={project.data.memberCount} note="Miembros del proyecto" />
          </section>

          {tasks.isError && <BoardError message={getApiError(tasks.error)} />}
          {tasks.isLoading && <KanbanLoading />}
          {tasks.data && (
            <section className="overflow-x-auto pb-3">
              <div className="grid min-w-[1390px] grid-cols-5 gap-4">
                {columns.map((column) => {
                  const columnTasks = tasksByStatus.get(column.status) ?? []
                  return (
                    <div key={column.status} className="flex min-h-[520px] flex-col rounded-2xl border border-white/8 bg-[#0b101a]/74 p-3" onDragOver={(event) => { event.preventDefault(); event.dataTransfer.dropEffect = 'move' }} onDrop={(event) => { event.preventDefault(); if (draggedTaskId) moveTask(draggedTaskId, column.status); setDraggedTaskId(null) }}>
                      <div className="flex items-center justify-between px-2 py-2"><div className="flex items-center gap-2"><span className={`h-2 w-2 rounded-full ${column.marker}`} /><h2 className="text-sm font-bold text-white">{column.label}</h2></div><span className="grid h-6 min-w-6 place-items-center rounded-md bg-white/6 px-1 text-xs font-bold text-slate-400">{columnTasks.length}</span></div>
                      <div className="mt-2 flex flex-1 flex-col gap-3">
                        {columnTasks.map((task) => <TaskCard key={task.id} task={task} onOpen={() => setSelectedTask(task)} onDragStart={() => setDraggedTaskId(task.id)} onDragEnd={() => setDraggedTaskId(null)} onStatusChange={(status) => moveTask(task.id, status)} />)}
                        {!columnTasks.length && <div className="grid min-h-24 place-items-center rounded-xl border border-dashed border-white/8 px-4 text-center text-xs leading-5 text-slate-600">Suelta una tarea aqui</div>}
                      </div>
                    </div>
                  )
                })}
              </div>
            </section>
          )}

          <section className="flex flex-wrap items-center gap-x-5 gap-y-2 rounded-2xl border border-brand-400/10 bg-brand-500/[0.045] px-5 py-4 text-xs text-slate-400"><span className="font-bold text-brand-300">Como funciona</span><span>Arrastra tarjetas entre columnas para actualizar su estado.</span><span>Tambien puedes cambiarlo desde el selector en cada tarea.</span></section>
        </>
      )}
      <TaskCreateModal projectId={projectId} workspaceId={workspaceId} open={taskModalOpen} onClose={() => setTaskModalOpen(false)} />
      <TaskDetailModal task={selectedTask} pending={updateStatus.isPending} onClose={() => setSelectedTask(null)} onStatusChange={(status) => selectedTask && moveTask(selectedTask.id, status)} />
    </div>
  )
}

function TaskCard({ task, onOpen, onDragStart, onDragEnd, onStatusChange }: { task: Task; onOpen: () => void; onDragStart: () => void; onDragEnd: () => void; onStatusChange: (status: TaskStatus) => void }) {
  return (
    <article draggable className="group cursor-grab rounded-xl border border-white/8 bg-[#111827] p-4 shadow-lg shadow-black/10 transition hover:border-brand-400/30 hover:bg-[#151d2c] active:cursor-grabbing" onDragStart={onDragStart} onDragEnd={onDragEnd}>
      <button className="w-full text-left" onClick={onOpen}><div className="flex items-start gap-2"><GripVertical size={16} className="mt-0.5 shrink-0 text-slate-600 transition group-hover:text-brand-400" /><p className="line-clamp-2 text-sm font-bold leading-5 text-white">{task.title}</p></div>{task.description && <p className="mt-3 line-clamp-2 text-xs leading-5 text-slate-500">{task.description}</p>}</button>
      <div className="mt-4 flex items-center justify-between gap-2"><span className={`text-xs font-bold ${priorityStyle[task.priority]}`}>{priorityCopy[task.priority]}</span>{task.dueDate && <span className="flex items-center gap-1 text-[11px] text-slate-500"><Clock3 size={13} />{formatShortDate(task.dueDate)}</span>}</div>
      <div className="mt-4 border-t border-white/6 pt-3"><label className="sr-only" htmlFor={`task-status-${task.id}`}>Cambiar estado de {task.title}</label><select id={`task-status-${task.id}`} className="w-full rounded-lg border border-white/8 bg-[#0d121e] px-2.5 py-2 text-[11px] font-bold text-slate-400 outline-none transition focus:border-brand-400" value={task.status} onClick={(event) => event.stopPropagation()} onChange={(event) => onStatusChange(event.target.value as TaskStatus)}>{columns.map((column) => <option key={column.status} value={column.status}>{column.label}</option>)}</select></div>
    </article>
  )
}

function Metric({ icon: Icon, label, value, note }: { icon: typeof ListTodo; label: string; value: number; note: string }) {
  return <article className="surface-card p-5"><div className="flex items-center justify-between"><p className="text-xs font-semibold text-slate-500">{label}</p><Icon size={17} className="text-brand-400" /></div><p className="mt-5 text-3xl font-semibold tracking-tight text-white">{value}</p><p className="mt-1 text-xs text-slate-500">{note}</p></article>
}

function BoardLoading() {
  return <div className="space-y-5"><div className="h-36 animate-pulse rounded-2xl bg-white/5" /><div className="grid gap-4 sm:grid-cols-3">{[1, 2, 3].map((item) => <div key={item} className="h-32 animate-pulse rounded-2xl bg-white/5" />)}</div></div>
}

function KanbanLoading() {
  return <section className="overflow-hidden"><div className="grid min-w-[1390px] grid-cols-5 gap-4">{columns.map((column) => <div key={column.status} className="h-[520px] animate-pulse rounded-2xl bg-white/5" />)}</div></section>
}

function BoardError({ message }: { message: string }) {
  return <section className="surface-card p-8"><p className="eyebrow text-red-300">No fue posible abrir el tablero</p><h1 className="mt-3 text-2xl font-semibold text-white">Revisa el acceso al proyecto</h1><p className="mt-3 text-sm text-slate-400">{message}</p></section>
}

function formatShortDate(date: string) {
  return new Intl.DateTimeFormat('es-MX', { day: 'numeric', month: 'short' }).format(new Date(`${date}T12:00:00`))
}
