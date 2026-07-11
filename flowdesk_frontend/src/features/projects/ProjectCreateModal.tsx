import { useEffect, useState, type FormEvent } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { FolderPlus, LoaderCircle, X } from 'lucide-react'
import { getApiError } from '../../lib/api'
import type { Project } from '../../types/project'
import { createProject } from './project-api'

interface ProjectCreateModalProps {
  workspaceId: string
  open: boolean
  onClose: () => void
}

export function ProjectCreateModal({ workspaceId, open, onClose }: ProjectCreateModalProps) {
  const queryClient = useQueryClient()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [hourlyRate, setHourlyRate] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')

  const mutation = useMutation({
    mutationFn: (payload: Parameters<typeof createProject>[1]) => createProject(workspaceId, payload),
    onSuccess: (project) => {
      queryClient.setQueryData<Project[]>(['projects', workspaceId], (current) => [project, ...(current ?? [])])
      setName('')
      setDescription('')
      setHourlyRate('')
      setStartDate('')
      setEndDate('')
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
    mutation.mutate({
      name: name.trim(),
      description: description.trim() || null,
      hourlyRate: Number(hourlyRate),
      startDate: startDate || null,
      endDate: endDate || null,
    })
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/72 p-5 backdrop-blur-sm" role="presentation" onMouseDown={(event) => {
      if (event.currentTarget === event.target && !mutation.isPending) onClose()
    }}>
      <section aria-modal="true" aria-labelledby="project-title" className="surface-card w-full max-w-2xl p-6 shadow-2xl" role="dialog">
        <div className="flex items-start justify-between gap-5">
          <div>
            <p className="eyebrow">Nuevo proyecto</p>
            <h2 id="project-title" className="mt-2 text-2xl font-semibold tracking-tight text-white">Define el siguiente resultado</h2>
            <p className="mt-2 text-sm leading-6 text-slate-400">El proyecto comenzara activo y podras organizar sus tareas en Kanban.</p>
          </div>
          <button aria-label="Cerrar" className="rounded-lg p-2 text-slate-400 hover:bg-white/6 hover:text-white" disabled={mutation.isPending} onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <form className="mt-7 grid gap-5 sm:grid-cols-2" onSubmit={handleSubmit}>
          <label className="block sm:col-span-2">
            <span className="mb-2 block text-xs font-semibold text-slate-300">Nombre del proyecto</span>
            <input autoFocus className="field" value={name} onChange={(event) => setName(event.target.value)} placeholder="Ej. Sitio web para Acme" required maxLength={100} />
          </label>
          <label className="block sm:col-span-2">
            <span className="mb-2 block text-xs font-semibold text-slate-300">Descripcion <span className="font-normal text-slate-500">(opcional)</span></span>
            <textarea className="field min-h-24 resize-y" value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Que objetivo tiene este proyecto?" />
          </label>
          <label className="block">
            <span className="mb-2 block text-xs font-semibold text-slate-300">Tarifa por hora</span>
            <input className="field" type="number" min="0.01" step="0.01" inputMode="decimal" value={hourlyRate} onChange={(event) => setHourlyRate(event.target.value)} placeholder="Ej. 450" required />
          </label>
          <div className="hidden sm:block" />
          <label className="block">
            <span className="mb-2 block text-xs font-semibold text-slate-300">Fecha de inicio <span className="font-normal text-slate-500">(opcional)</span></span>
            <input className="field" type="date" value={startDate} onChange={(event) => setStartDate(event.target.value)} />
          </label>
          <label className="block">
            <span className="mb-2 block text-xs font-semibold text-slate-300">Fecha de entrega <span className="font-normal text-slate-500">(opcional)</span></span>
            <input className="field" type="date" min={startDate || undefined} value={endDate} onChange={(event) => setEndDate(event.target.value)} />
          </label>

          {mutation.isError && <p className="rounded-xl border border-red-400/15 bg-red-400/8 px-4 py-3 text-sm text-red-300 sm:col-span-2">{getApiError(mutation.error)}</p>}

          <div className="flex flex-col-reverse gap-3 pt-2 sm:col-span-2 sm:flex-row sm:justify-end">
            <button type="button" className="rounded-xl px-4 py-3 text-sm font-bold text-slate-400 hover:bg-white/5 hover:text-white" disabled={mutation.isPending} onClick={onClose}>Cancelar</button>
            <button className="flex items-center justify-center gap-2 rounded-xl bg-brand-500 px-5 py-3 text-sm font-bold text-white hover:bg-brand-400 disabled:cursor-wait disabled:opacity-60" disabled={mutation.isPending}>
              {mutation.isPending ? <LoaderCircle size={17} className="animate-spin" /> : <FolderPlus size={17} />}
              {mutation.isPending ? 'Creando...' : 'Crear proyecto'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
