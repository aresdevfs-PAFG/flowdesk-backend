import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { ArrowRight, CalendarDays, FolderKanban, FolderPlus, LayoutList, UsersRound } from 'lucide-react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { getApiError } from '../../lib/api'
import { getProjects } from '../projects/project-api'
import { ProjectCreateModal } from '../projects/ProjectCreateModal'
import { useWorkspaceStore } from '../../stores/workspace-store'
import type { Project, ProjectStatus } from '../../types/project'
import { getWorkspace } from './workspace-api'

const statusCopy: Record<ProjectStatus, string> = {
  ACTIVE: 'Activo',
  ON_HOLD: 'En pausa',
  COMPLETED: 'Completado',
  CANCELLED: 'Cancelado',
}

const statusStyle: Record<ProjectStatus, string> = {
  ACTIVE: 'bg-emerald-400/10 text-emerald-300 ring-emerald-400/20',
  ON_HOLD: 'bg-amber-400/10 text-amber-300 ring-amber-400/20',
  COMPLETED: 'bg-brand-500/10 text-brand-300 ring-brand-400/20',
  CANCELLED: 'bg-slate-400/10 text-slate-400 ring-slate-400/20',
}

export function WorkspacePage() {
  const { workspaceId } = useParams()
  const setActiveWorkspaceId = useWorkspaceStore((state) => state.setActiveWorkspaceId)
  const [projectModalOpen, setProjectModalOpen] = useState(false)
  const workspace = useQuery({ queryKey: ['workspace', workspaceId], queryFn: () => getWorkspace(workspaceId!), enabled: Boolean(workspaceId) })
  const projects = useQuery({ queryKey: ['projects', workspaceId], queryFn: () => getProjects(workspaceId!), enabled: Boolean(workspaceId) })

  useEffect(() => {
    if (workspaceId) setActiveWorkspaceId(workspaceId)
  }, [setActiveWorkspaceId, workspaceId])

  if (!workspaceId) return <Navigate to="/" replace />

  const totalTasks = projects.data?.reduce((total, project) => total + project.taskCount, 0) ?? 0
  const activeProjects = projects.data?.filter((project) => project.status === 'ACTIVE').length ?? 0

  return (
    <div className="space-y-8">
      {workspace.isLoading && <WorkspaceLoading />}
      {workspace.isError && <ErrorState message={getApiError(workspace.error)} />}
      {workspace.data && (
        <>
          <section className="overflow-hidden rounded-3xl border border-white/10 bg-gradient-to-br from-brand-500/18 via-[#151b35] to-[#101521] p-6 sm:p-8">
            <div className="flex flex-col justify-between gap-8 lg:flex-row lg:items-end">
              <div className="flex items-start gap-5">
                <div className="grid h-16 w-16 shrink-0 place-items-center rounded-2xl bg-brand-500 text-xl font-extrabold text-white shadow-lg shadow-brand-500/20">{workspace.data.name.slice(0, 2).toUpperCase()}</div>
                <div>
                  <p className="eyebrow text-brand-300">Workspace</p>
                  <h1 className="mt-2 text-3xl font-semibold tracking-[-0.045em] text-white sm:text-4xl">{workspace.data.name}</h1>
                  <p className="mt-3 flex flex-wrap items-center gap-x-2 gap-y-1 text-sm text-slate-400"><span>flowdesk/{workspace.data.slug}</span><span className="text-slate-600">/</span><span>Creado por {workspace.data.ownerName}</span></p>
                </div>
              </div>
              <button onClick={() => setProjectModalOpen(true)} className="flex items-center justify-center gap-2 rounded-xl bg-white px-4 py-3 text-sm font-bold text-slate-950 transition hover:bg-slate-200">
                <FolderPlus size={17} /> Nuevo proyecto
              </button>
            </div>
          </section>

          <section className="grid gap-4 sm:grid-cols-3">
            <Metric icon={FolderKanban} label="Proyectos activos" value={activeProjects} note={`${projects.data?.length ?? 0} proyectos en total`} />
            <Metric icon={LayoutList} label="Tareas" value={totalTasks} note="En todos los proyectos" />
            <Metric icon={UsersRound} label="Equipo" value={workspace.data.memberCount} note="Miembros del workspace" />
          </section>

          <section className="surface-card overflow-hidden">
            <div className="flex flex-col gap-4 border-b border-white/8 px-6 py-5 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <p className="text-sm font-bold text-white">Proyectos</p>
                <p className="mt-1 text-xs text-slate-500">El trabajo organizado de {workspace.data.name}.</p>
              </div>
              <button onClick={() => setProjectModalOpen(true)} className="flex items-center gap-2 self-start text-xs font-bold text-brand-400 hover:text-brand-300 sm:self-auto"><FolderPlus size={15} /> Crear proyecto</button>
            </div>

            {projects.isLoading && <p className="px-6 py-10 text-sm text-slate-500">Cargando proyectos...</p>}
            {projects.isError && <p className="px-6 py-10 text-sm text-red-300">{getApiError(projects.error)}</p>}
            {projects.data?.length === 0 && (
              <div className="px-6 py-14 text-center">
                <div className="mx-auto grid h-12 w-12 place-items-center rounded-2xl bg-brand-500/10 text-brand-400"><FolderKanban size={22} /></div>
                <h2 className="mt-4 text-lg font-bold text-white">Tu workspace esta listo para empezar</h2>
                <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-slate-400">Crea un proyecto para definir su tarifa, fechas y despues llevarlo al tablero Kanban.</p>
                <button onClick={() => setProjectModalOpen(true)} className="mt-6 inline-flex items-center gap-2 rounded-xl bg-brand-500 px-4 py-3 text-sm font-bold text-white hover:bg-brand-400"><FolderPlus size={17} /> Crear el primer proyecto</button>
              </div>
            )}
            {projects.data?.length ? <div className="grid gap-px bg-white/6 md:grid-cols-2">{projects.data.map((project) => <ProjectCard key={project.id} project={project} />)}</div> : null}
          </section>

          <section className="grid gap-4 lg:grid-cols-[1fr_0.78fr]">
            <article className="surface-card p-6">
              <div className="flex items-center gap-3"><div className="grid h-10 w-10 place-items-center rounded-xl bg-cyan-400/10 text-cyan-300"><UsersRound size={19} /></div><div><h2 className="text-sm font-bold text-white">Equipo</h2><p className="mt-1 text-xs text-slate-500">{workspace.data.memberCount} miembros en este workspace</p></div></div>
              <p className="mt-5 text-sm leading-6 text-slate-400">La gestion de miembros ya esta disponible en el backend. La vista de invitaciones sera el siguiente complemento de este espacio.</p>
            </article>
            <article className="surface-card p-6">
              <div className="flex items-center gap-3"><div className="grid h-10 w-10 place-items-center rounded-xl bg-amber-400/10 text-amber-300"><CalendarDays size={19} /></div><div><h2 className="text-sm font-bold text-white">Ritmo de trabajo</h2><p className="mt-1 text-xs text-slate-500">Progreso conectado a proyectos</p></div></div>
              <p className="mt-5 text-sm leading-6 text-slate-400">Cuando creemos las tareas, este panel mostrara entregas, carga y actividad del equipo.</p>
            </article>
          </section>
        </>
      )}
      <ProjectCreateModal workspaceId={workspaceId} open={projectModalOpen} onClose={() => setProjectModalOpen(false)} />
    </div>
  )
}

function ProjectCard({ project }: { project: Project }) {
  return (
    <Link to={`/workspaces/${project.workspaceId}/projects/${project.id}`} className="group block bg-[#0d121e] p-6 transition hover:bg-[#121929]">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0"><p className="truncate text-base font-bold text-white">{project.name}</p><p className="mt-2 line-clamp-2 min-h-10 text-sm leading-5 text-slate-400">{project.description || 'Sin descripcion todavia.'}</p></div>
        <span className={`shrink-0 rounded-full px-2.5 py-1 text-[10px] font-extrabold uppercase tracking-wider ring-1 ${statusStyle[project.status]}`}>{statusCopy[project.status]}</span>
      </div>
      <div className="mt-6 flex flex-wrap items-center gap-x-4 gap-y-2 border-t border-white/6 pt-4 text-xs text-slate-500">
        <span>{project.taskCount} tareas</span><span>{project.memberCount} miembros</span><span>{formatRate(project.hourlyRate)}/h</span>
      </div>
      <div className="mt-5 flex items-center justify-between text-xs"><span className="text-slate-500">{formatDates(project)}</span><span className="flex items-center gap-1 font-bold text-brand-400">Abrir Kanban <ArrowRight size={14} /></span></div>
    </Link>
  )
}

function Metric({ icon: Icon, label, value, note }: { icon: typeof FolderKanban; label: string; value: number; note: string }) {
  return <article className="surface-card p-5"><div className="flex items-center justify-between"><p className="text-xs font-semibold text-slate-500">{label}</p><Icon size={17} className="text-brand-400" /></div><p className="mt-5 text-3xl font-semibold tracking-tight text-white">{value}</p><p className="mt-1 text-xs text-slate-500">{note}</p></article>
}

function WorkspaceLoading() {
  return <div className="space-y-5"><div className="h-48 animate-pulse rounded-3xl bg-white/5" /><div className="grid gap-4 sm:grid-cols-3">{[1, 2, 3].map((item) => <div key={item} className="h-32 animate-pulse rounded-2xl bg-white/5" />)}</div><div className="h-64 animate-pulse rounded-2xl bg-white/5" /></div>
}

function ErrorState({ message }: { message: string }) {
  return <section className="surface-card p-8"><p className="eyebrow text-red-300">No fue posible abrir el workspace</p><h1 className="mt-3 text-2xl font-semibold text-white">Revisa el acceso al espacio</h1><p className="mt-3 text-sm text-slate-400">{message}</p></section>
}

function formatRate(rate: number) {
  return new Intl.NumberFormat('es-MX', { maximumFractionDigits: 2 }).format(rate)
}

function formatDates(project: Project) {
  if (project.startDate && project.endDate) return `${formatDate(project.startDate)} - ${formatDate(project.endDate)}`
  if (project.startDate) return `Inicio: ${formatDate(project.startDate)}`
  if (project.endDate) return `Entrega: ${formatDate(project.endDate)}`
  return 'Sin fechas definidas'
}

function formatDate(date: string) {
  return new Intl.DateTimeFormat('es-MX', { day: 'numeric', month: 'short' }).format(new Date(`${date}T12:00:00`))
}
