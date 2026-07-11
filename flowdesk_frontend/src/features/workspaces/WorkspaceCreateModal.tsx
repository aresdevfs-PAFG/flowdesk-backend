import { useEffect, useState, type FormEvent } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { LoaderCircle, Plus, X } from 'lucide-react'
import { getApiError } from '../../lib/api'
import { useWorkspaceStore } from '../../stores/workspace-store'
import type { Workspace } from '../../types/workspace'
import { createWorkspace } from './workspace-api'

interface WorkspaceCreateModalProps {
  open: boolean
  onClose: () => void
}

export function WorkspaceCreateModal({ open, onClose }: WorkspaceCreateModalProps) {
  const queryClient = useQueryClient()
  const setActiveWorkspaceId = useWorkspaceStore((state) => state.setActiveWorkspaceId)
  const [name, setName] = useState('')
  const [slug, setSlug] = useState('')
  const [slugEdited, setSlugEdited] = useState(false)

  const mutation = useMutation({
    mutationFn: createWorkspace,
    onSuccess: (workspace) => {
      queryClient.setQueryData<Workspace[]>(['workspaces'], (current) => [
        ...(current ?? []),
        workspace,
      ])
      setActiveWorkspaceId(workspace.id)
      setName('')
      setSlug('')
      setSlugEdited(false)
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

  function handleNameChange(value: string) {
    setName(value)
    if (!slugEdited) setSlug(slugify(value))
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    mutation.mutate({ name: name.trim(), slug: slugify(slug) })
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/72 p-5 backdrop-blur-sm" role="presentation" onMouseDown={(event) => {
      if (event.currentTarget === event.target && !mutation.isPending) onClose()
    }}>
      <section aria-modal="true" aria-labelledby="workspace-title" className="surface-card w-full max-w-lg p-6 shadow-2xl" role="dialog">
        <div className="flex items-start justify-between gap-5">
          <div>
            <p className="eyebrow">Nuevo espacio</p>
            <h2 id="workspace-title" className="mt-2 text-2xl font-semibold tracking-tight text-white">Crea un workspace</h2>
            <p className="mt-2 text-sm leading-6 text-slate-400">Tu cuenta sera administradora y podras invitar al equipo despues.</p>
          </div>
          <button aria-label="Cerrar" className="rounded-lg p-2 text-slate-400 hover:bg-white/6 hover:text-white" disabled={mutation.isPending} onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <form className="mt-7 space-y-5" onSubmit={handleSubmit}>
          <label className="block">
            <span className="mb-2 block text-xs font-semibold text-slate-300">Nombre del workspace</span>
            <input autoFocus className="field" value={name} onChange={(event) => handleNameChange(event.target.value)} placeholder="Ej. Estudio Norte" required maxLength={100} />
          </label>
          <label className="block">
            <span className="mb-2 block text-xs font-semibold text-slate-300">Identificador</span>
            <div className="flex overflow-hidden rounded-xl border border-white/10 bg-[#080b12]/65 focus-within:border-brand-400/75 focus-within:ring-3 focus-within:ring-brand-500/12">
              <span className="flex items-center border-r border-white/8 px-3 text-xs font-semibold text-slate-500">flowdesk/</span>
              <input className="min-w-0 flex-1 bg-transparent px-3 py-3 text-sm text-white outline-none" value={slug}
                onChange={(event) => { setSlugEdited(true); setSlug(slugify(event.target.value)) }} placeholder="estudio-norte" required maxLength={100} pattern="[a-z0-9-]+" />
            </div>
            <span className="mt-2 block text-xs text-slate-500">Solo minusculas, numeros y guiones.</span>
          </label>

          {mutation.isError && <p className="rounded-xl border border-red-400/15 bg-red-400/8 px-4 py-3 text-sm text-red-300">{getApiError(mutation.error)}</p>}

          <div className="flex flex-col-reverse gap-3 pt-2 sm:flex-row sm:justify-end">
            <button type="button" className="rounded-xl px-4 py-3 text-sm font-bold text-slate-400 hover:bg-white/5 hover:text-white" disabled={mutation.isPending} onClick={onClose}>Cancelar</button>
            <button className="flex items-center justify-center gap-2 rounded-xl bg-brand-500 px-5 py-3 text-sm font-bold text-white hover:bg-brand-400 disabled:cursor-wait disabled:opacity-60" disabled={mutation.isPending}>
              {mutation.isPending ? <LoaderCircle size={17} className="animate-spin" /> : <Plus size={17} />}
              {mutation.isPending ? 'Creando...' : 'Crear workspace'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

function slugify(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
}
