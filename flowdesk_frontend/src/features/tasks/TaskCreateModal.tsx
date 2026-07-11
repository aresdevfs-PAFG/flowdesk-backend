import { useEffect, useState, type FormEvent } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { CheckSquare, LoaderCircle, X } from 'lucide-react'
import { getApiError } from '../../lib/api'
import type { Project } from '../../types/project'
import type { Task, TaskPriority } from '../../types/task'
import { createTask } from './task-api'

interface TaskCreateModalProps {
  projectId: string
  workspaceId: string
  open: boolean
  onClose: () => void
}

const priorities: { value: TaskPriority; label: string }[] = [
  { value: 'LOW', label: 'Baja' },
  { value: 'MEDIUM', label: 'Media' },
  { value: 'HIGH', label: 'Alta' },
  { value: 'CRITICAL', label: 'Critica' },
]

export function TaskCreateModal({ projectId, workspaceId, open, onClose }: TaskCreateModalProps) {
  const queryClient = useQueryClient()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM')
  const [dueDate, setDueDate] = useState('')

  const mutation = useMutation({
    mutationFn: (payload: Parameters<typeof createTask>[1]) => createTask(projectId, payload),
    onSuccess: (task) => {
      queryClient.setQueryData<Task[]>(['tasks', projectId], (current) => [...(current ?? []), task])
      queryClient.setQueryData<Project>(['project', workspaceId, projectId], (current) => current ? { ...current, taskCount: current.taskCount + 1 } : current)
      queryClient.invalidateQueries({ queryKey: ['projects', workspaceId] })
      setTitle('')
      setDescription('')
      setPriority('MEDIUM')
      setDueDate('')
      onClose()
    },
  })

  useEffect(() => {
    if (!open) return
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !mutation.isPending) onClose()
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [mutation.isPending, onClose, open])

  if (!open) return null

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    mutation.mutate({ title: title.trim(), description: description.trim() || null, priority, assigneeId: null, dueDate: dueDate || null })
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/72 p-5 backdrop-blur-sm" role="presentation" onMouseDown={(event) => {
      if (event.currentTarget === event.target && !mutation.isPending) onClose()
    }}>
      <section aria-modal="true" aria-labelledby="task-title" className="surface-card w-full max-w-2xl p-6 shadow-2xl" role="dialog">
        <div className="flex items-start justify-between gap-5">
          <div>
            <p className="eyebrow">Nueva tarea</p>
            <h2 id="task-title" className="mt-2 text-2xl font-semibold tracking-tight text-white">Convierte el trabajo en una accion clara</h2>
            <p className="mt-2 text-sm leading-6 text-slate-400">La tarea aparecera en la columna Por hacer. Podras moverla en el tablero despues.</p>
          </div>
          <button aria-label="Cerrar" className="rounded-lg p-2 text-slate-400 hover:bg-white/6 hover:text-white" disabled={mutation.isPending} onClick={onClose}><X size={18} /></button>
        </div>

        <form className="mt-7 grid gap-5 sm:grid-cols-2" onSubmit={handleSubmit}>
          <label className="block sm:col-span-2">
            <span className="mb-2 block text-xs font-semibold text-slate-300">Titulo de la tarea</span>
            <input autoFocus className="field" value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Ej. Preparar propuesta inicial" required maxLength={200} />
          </label>
          <label className="block sm:col-span-2">
            <span className="mb-2 block text-xs font-semibold text-slate-300">Contexto <span className="font-normal text-slate-500">(opcional)</span></span>
            <textarea className="field min-h-28 resize-y" value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Que se necesita hacer para dar esta tarea por terminada?" />
          </label>
          <label className="block">
            <span className="mb-2 block text-xs font-semibold text-slate-300">Prioridad</span>
            <select className="field" value={priority} onChange={(event) => setPriority(event.target.value as TaskPriority)}>
              {priorities.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}
            </select>
          </label>
          <label className="block">
            <span className="mb-2 block text-xs font-semibold text-slate-300">Fecha limite <span className="font-normal text-slate-500">(opcional)</span></span>
            <input className="field" type="date" value={dueDate} onChange={(event) => setDueDate(event.target.value)} />
          </label>

          {mutation.isError && <p className="rounded-xl border border-red-400/15 bg-red-400/8 px-4 py-3 text-sm text-red-300 sm:col-span-2">{getApiError(mutation.error)}</p>}

          <div className="flex flex-col-reverse gap-3 pt-2 sm:col-span-2 sm:flex-row sm:justify-end">
            <button type="button" className="rounded-xl px-4 py-3 text-sm font-bold text-slate-400 hover:bg-white/5 hover:text-white" disabled={mutation.isPending} onClick={onClose}>Cancelar</button>
            <button className="flex items-center justify-center gap-2 rounded-xl bg-brand-500 px-5 py-3 text-sm font-bold text-white hover:bg-brand-400 disabled:cursor-wait disabled:opacity-60" disabled={mutation.isPending}>
              {mutation.isPending ? <LoaderCircle size={17} className="animate-spin" /> : <CheckSquare size={17} />}
              {mutation.isPending ? 'Creando...' : 'Crear tarea'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
