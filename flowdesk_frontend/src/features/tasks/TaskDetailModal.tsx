import { CalendarDays, CircleUserRound, X } from 'lucide-react'
import type { Task, TaskPriority, TaskStatus } from '../../types/task'

interface TaskDetailModalProps {
  task: Task | null
  pending: boolean
  onClose: () => void
  onStatusChange: (status: TaskStatus) => void
}

const priorityCopy: Record<TaskPriority, string> = {
  LOW: 'Baja',
  MEDIUM: 'Media',
  HIGH: 'Alta',
  CRITICAL: 'Critica',
}

const statusCopy: Record<TaskStatus, string> = {
  TODO: 'Por hacer',
  IN_PROGRESS: 'En progreso',
  IN_REVIEW: 'En revision',
  DONE: 'Completado',
  CANCELLED: 'Cancelado',
}

export function TaskDetailModal({ task, pending, onClose, onStatusChange }: TaskDetailModalProps) {
  if (!task) return null

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/72 p-5 backdrop-blur-sm" role="presentation" onMouseDown={(event) => {
      if (event.currentTarget === event.target && !pending) onClose()
    }}>
      <section aria-modal="true" aria-labelledby="task-detail-title" className="surface-card w-full max-w-xl p-6 shadow-2xl" role="dialog">
        <div className="flex items-start justify-between gap-5">
          <div className="min-w-0"><p className="eyebrow">Detalle de tarea</p><h2 id="task-detail-title" className="mt-2 text-2xl font-semibold tracking-tight text-white">{task.title}</h2></div>
          <button aria-label="Cerrar" className="rounded-lg p-2 text-slate-400 hover:bg-white/6 hover:text-white" disabled={pending} onClick={onClose}><X size={18} /></button>
        </div>

        <p className="mt-6 whitespace-pre-wrap text-sm leading-6 text-slate-300">{task.description || 'Esta tarea aun no tiene una descripcion.'}</p>

        <div className="mt-7 grid gap-4 border-y border-white/8 py-5 sm:grid-cols-2">
          <label className="block"><span className="mb-2 block text-xs font-semibold text-slate-500">Estado</span><select className="field py-2.5 text-sm" value={task.status} disabled={pending} onChange={(event) => onStatusChange(event.target.value as TaskStatus)}>{Object.entries(statusCopy).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
          <div><p className="mb-2 text-xs font-semibold text-slate-500">Prioridad</p><p className="flex h-11 items-center rounded-xl border border-white/10 bg-white/[0.025] px-3 text-sm font-semibold text-white">{priorityCopy[task.priority]}</p></div>
          <div><p className="mb-2 text-xs font-semibold text-slate-500">Responsable</p><p className="flex h-11 items-center gap-2 rounded-xl border border-white/10 bg-white/[0.025] px-3 text-sm text-slate-300"><CircleUserRound size={16} className="text-slate-500" />{task.assignee?.name ?? 'Sin asignar'}</p></div>
          <div><p className="mb-2 text-xs font-semibold text-slate-500">Fecha limite</p><p className="flex h-11 items-center gap-2 rounded-xl border border-white/10 bg-white/[0.025] px-3 text-sm text-slate-300"><CalendarDays size={16} className="text-slate-500" />{task.dueDate ? formatDate(task.dueDate) : 'Sin fecha definida'}</p></div>
        </div>

        <div className="mt-6 flex justify-end"><button className="rounded-xl bg-white px-4 py-3 text-sm font-bold text-slate-950 hover:bg-slate-200" onClick={onClose}>Cerrar detalle</button></div>
      </section>
    </div>
  )
}

function formatDate(date: string) {
  return new Intl.DateTimeFormat('es-MX', { day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(`${date}T12:00:00`))
}
