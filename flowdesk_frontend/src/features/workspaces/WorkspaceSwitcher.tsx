import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { ChevronDown, Plus } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useWorkspaceStore } from '../../stores/workspace-store'
import { getWorkspaces } from './workspace-api'

interface WorkspaceSwitcherProps {
  onCreateWorkspace: () => void
}

export function WorkspaceSwitcher({ onCreateWorkspace }: WorkspaceSwitcherProps) {
  const activeWorkspaceId = useWorkspaceStore((state) => state.activeWorkspaceId)
  const setActiveWorkspaceId = useWorkspaceStore((state) => state.setActiveWorkspaceId)
  const navigate = useNavigate()
  const workspaces = useQuery({ queryKey: ['workspaces'], queryFn: getWorkspaces })
  const activeWorkspace = workspaces.data?.find((workspace) => workspace.id === activeWorkspaceId)

  useEffect(() => {
    if (workspaces.data?.length && !activeWorkspace) setActiveWorkspaceId(workspaces.data[0].id)
  }, [activeWorkspace, setActiveWorkspaceId, workspaces.data])

  return (
    <div className="flex min-w-0 items-center gap-2">
      <label className="sr-only" htmlFor="workspace-switcher">Workspace activo</label>
      <div className="relative min-w-0">
        <select id="workspace-switcher" className="max-w-44 appearance-none rounded-xl border border-white/8 bg-[#101624] py-2 pl-3 pr-8 text-sm font-bold text-white outline-none transition hover:border-white/16 focus:border-brand-400 sm:max-w-64"
          value={activeWorkspaceId ?? ''} disabled={workspaces.isLoading || !workspaces.data?.length}
          onChange={(event) => {
            setActiveWorkspaceId(event.target.value)
            navigate(`/workspaces/${event.target.value}`)
          }}>
          {!workspaces.data?.length && <option value="">{workspaces.isLoading ? 'Cargando...' : 'Sin workspace'}</option>}
          {workspaces.data?.map((workspace) => <option key={workspace.id} value={workspace.id}>{workspace.name}</option>)}
        </select>
        <ChevronDown className="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 text-slate-400" size={15} />
      </div>
      <button aria-label="Crear workspace" title="Crear workspace" className="rounded-xl border border-white/8 p-2 text-brand-400 transition hover:border-brand-400/40 hover:bg-brand-500/10" onClick={onCreateWorkspace}>
        <Plus size={18} />
      </button>
    </div>
  )
}
