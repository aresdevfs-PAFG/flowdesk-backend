import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { ArrowUpRight, BriefcaseBusiness, Clock3, Plus, Users } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../stores/auth-store'
import { useWorkspaceStore } from '../../stores/workspace-store'
import { WorkspaceCreateModal } from '../workspaces/WorkspaceCreateModal'
import { getWorkspaces } from '../workspaces/workspace-api'

export function DashboardPage() {
  const name = useAuthStore((state) => state.session?.name.split(' ')[0])
  const activeWorkspaceId = useWorkspaceStore((state) => state.activeWorkspaceId)
  const setActiveWorkspaceId = useWorkspaceStore((state) => state.setActiveWorkspaceId)
  const navigate = useNavigate()
  const [workspaceModalOpen, setWorkspaceModalOpen] = useState(false)
  const workspaces = useQuery({ queryKey: ['workspaces'], queryFn: getWorkspaces })
  const totalMembers = workspaces.data?.reduce((total, item) => total + item.memberCount, 0) ?? 0

  useEffect(() => {
    if (workspaces.data?.length && !workspaces.data.some((workspace) => workspace.id === activeWorkspaceId)) {
      setActiveWorkspaceId(workspaces.data[0].id)
    }
  }, [activeWorkspaceId, setActiveWorkspaceId, workspaces.data])

  return (
    <div className="space-y-8">
      <section className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="eyebrow">Panel operativo</p>
          <h1 className="mt-3 text-4xl font-semibold tracking-[-0.045em] text-white">Buen día, {name}.</h1>
          <p className="mt-2 text-sm text-slate-400">Aquí tienes el pulso de tus espacios de trabajo.</p>
        </div>
        <button onClick={() => setWorkspaceModalOpen(true)} className="flex items-center justify-center gap-2 rounded-xl bg-white px-4 py-3 text-sm font-bold text-slate-950 hover:bg-slate-200">
          <Plus size={17} /> Nuevo workspace
        </button>
      </section>

      <section className="grid gap-4 sm:grid-cols-3">
        <Metric icon={BriefcaseBusiness} label="Workspaces" value={workspaces.data?.length ?? 0} note="Espacios activos" />
        <Metric icon={Users} label="Colaboradores" value={totalMembers} note="En todos tus espacios" />
        <Metric icon={Clock3} label="Seguimiento" value="Listo" note="Timer conectado al backend" />
      </section>

      <section className="surface-card overflow-hidden">
        <div className="flex items-center justify-between border-b border-white/8 px-6 py-5">
          <div><p className="text-sm font-bold text-white">Tus workspaces</p><p className="mt-1 text-xs text-slate-500">Datos servidos por Spring Boot</p></div>
          <button className="text-xs font-bold text-brand-400">Ver todos</button>
        </div>
        <div className="divide-y divide-white/6">
          {workspaces.isLoading && <p className="px-6 py-8 text-sm text-slate-500">Cargando espacios…</p>}
          {workspaces.isError && <p className="px-6 py-8 text-sm text-red-300">No pudimos cargar los workspaces. Confirma que Spring esté ejecutándose en el puerto 8080.</p>}
          {workspaces.data?.map((workspace) => {
            const isActive = workspace.id === activeWorkspaceId
            return (
            <button key={workspace.id} onClick={() => { setActiveWorkspaceId(workspace.id); navigate(`/workspaces/${workspace.id}`) }} className={`flex w-full items-center gap-4 px-6 py-5 text-left transition hover:bg-white/[0.025] ${isActive ? 'bg-brand-500/[0.07]' : ''}`}>
              <div className="grid h-11 w-11 place-items-center rounded-xl bg-brand-500/12 text-sm font-extrabold text-brand-400">{workspace.name.slice(0, 2).toUpperCase()}</div>
              <div className="min-w-0 flex-1"><p className="truncate text-sm font-bold text-white">{workspace.name}</p><p className="mt-1 text-xs text-slate-500">{workspace.memberCount} miembros · {workspace.slug}</p></div>
              <div className="flex items-center gap-3">{isActive && <span className="hidden rounded-full bg-brand-500/15 px-2.5 py-1 text-[10px] font-extrabold uppercase tracking-wider text-brand-400 sm:block">Activo</span>}<ArrowUpRight size={17} className="text-slate-600" /></div>
            </button>
            )
          })}
          {workspaces.data?.length === 0 && <div className="px-6 py-8 text-sm text-slate-500"><p>Aun no tienes workspaces. Crea el primero para comenzar.</p><button onClick={() => setWorkspaceModalOpen(true)} className="mt-4 text-sm font-bold text-brand-400 hover:text-brand-300">Crear mi primer workspace</button></div>}
        </div>
      </section>
      <WorkspaceCreateModal open={workspaceModalOpen} onClose={() => setWorkspaceModalOpen(false)} />
    </div>
  )
}

function Metric({ icon: Icon, label, value, note }: { icon: typeof Clock3; label: string; value: string | number; note: string }) {
  return (
    <article className="surface-card p-5">
      <div className="flex items-center justify-between"><p className="text-xs font-semibold text-slate-500">{label}</p><Icon size={17} className="text-brand-400" /></div>
      <p className="mt-5 text-3xl font-semibold tracking-tight text-white">{value}</p>
      <p className="mt-1 text-xs text-slate-500">{note}</p>
    </article>
  )
}
