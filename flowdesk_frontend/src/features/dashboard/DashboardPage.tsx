import { useQuery } from '@tanstack/react-query'
import { ArrowUpRight, BriefcaseBusiness, Clock3, Plus, Users } from 'lucide-react'
import { api } from '../../lib/api'
import { useAuthStore } from '../../stores/auth-store'
import type { Workspace } from '../../types/workspace'

async function getWorkspaces() {
  const { data } = await api.get<Workspace[]>('/workspaces')
  return data
}

export function DashboardPage() {
  const name = useAuthStore((state) => state.session?.name.split(' ')[0])
  const workspaces = useQuery({ queryKey: ['workspaces'], queryFn: getWorkspaces })
  const totalMembers = workspaces.data?.reduce((total, item) => total + item.memberCount, 0) ?? 0

  return (
    <div className="space-y-8">
      <section className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="eyebrow">Panel operativo</p>
          <h1 className="mt-3 text-4xl font-semibold tracking-[-0.045em] text-white">Buen día, {name}.</h1>
          <p className="mt-2 text-sm text-slate-400">Aquí tienes el pulso de tus espacios de trabajo.</p>
        </div>
        <button className="flex items-center justify-center gap-2 rounded-xl bg-white px-4 py-3 text-sm font-bold text-slate-950 hover:bg-slate-200">
          <Plus size={17} /> Nuevo proyecto
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
          {workspaces.data?.map((workspace) => (
            <article key={workspace.id} className="flex items-center gap-4 px-6 py-5 transition hover:bg-white/[0.025]">
              <div className="grid h-11 w-11 place-items-center rounded-xl bg-brand-500/12 text-sm font-extrabold text-brand-400">{workspace.name.slice(0, 2).toUpperCase()}</div>
              <div className="min-w-0 flex-1"><p className="truncate text-sm font-bold text-white">{workspace.name}</p><p className="mt-1 text-xs text-slate-500">{workspace.memberCount} miembros · {workspace.slug}</p></div>
              <ArrowUpRight size={17} className="text-slate-600" />
            </article>
          ))}
          {workspaces.data?.length === 0 && <p className="px-6 py-8 text-sm text-slate-500">Aún no tienes workspaces. El siguiente paso será crear el primero desde esta interfaz.</p>}
        </div>
      </section>
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
