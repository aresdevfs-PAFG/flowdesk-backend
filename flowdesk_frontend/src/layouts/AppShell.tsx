import { BarChart3, Bell, BriefcaseBusiness, CreditCard, LayoutDashboard, LogOut, Search } from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuthStore } from '../stores/auth-store'

const navItems = [
  { to: '/', label: 'Resumen', icon: LayoutDashboard, end: true },
  { to: '/projects', label: 'Proyectos', icon: BriefcaseBusiness },
  { to: '/reports', label: 'Reportes', icon: BarChart3 },
  { to: '/billing', label: 'Facturación', icon: CreditCard },
]

export function AppShell() {
  const session = useAuthStore((state) => state.session)!
  const clearSession = useAuthStore((state) => state.clearSession)

  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[250px_1fr]">
      <aside className="border-b border-white/8 bg-[#0b0f18]/80 p-5 backdrop-blur-xl lg:sticky lg:top-0 lg:h-screen lg:border-b-0 lg:border-r">
        <div className="flex items-center justify-between lg:block">
          <div className="flex items-center gap-3 text-lg font-bold text-white">
            <span className="grid h-9 w-9 place-items-center rounded-xl bg-brand-500"><LayoutDashboard size={18} /></span>
            FlowDesk
          </div>
          <button className="text-slate-400 lg:hidden"><Bell size={20} /></button>
        </div>
        <nav className="mt-5 flex gap-2 overflow-x-auto lg:mt-10 lg:block lg:space-y-1">
          {navItems.map(({ to, label, icon: Icon, end }) => (
            <NavLink key={to} to={to} end={end}
              className={({ isActive }) => `flex min-w-max items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold transition ${isActive ? 'bg-brand-500/14 text-brand-400' : 'text-slate-400 hover:bg-white/5 hover:text-white'}`}>
              <Icon size={18} /> {label}
            </NavLink>
          ))}
        </nav>
        <div className="mt-8 hidden border-t border-white/8 pt-5 lg:block">
          <p className="truncate text-sm font-semibold text-white">{session.name}</p>
          <p className="mt-1 truncate text-xs text-slate-500">{session.email}</p>
          <button onClick={clearSession} className="mt-5 flex items-center gap-2 text-xs font-semibold text-slate-400 hover:text-white">
            <LogOut size={15} /> Cerrar sesión
          </button>
        </div>
      </aside>

      <main className="min-w-0">
        <header className="flex h-20 items-center justify-between border-b border-white/8 px-6 lg:px-10">
          <div className="hidden items-center gap-3 text-slate-500 sm:flex">
            <Search size={18} />
            <span className="text-sm">Buscar proyectos, tareas o personas</span>
          </div>
          <div className="ml-auto flex items-center gap-4">
            <button className="relative rounded-xl border border-white/8 p-2.5 text-slate-400 hover:text-white">
              <Bell size={18} />
              <span className="absolute right-2 top-2 h-1.5 w-1.5 rounded-full bg-brand-400" />
            </button>
            <div className="grid h-10 w-10 place-items-center rounded-xl bg-gradient-to-br from-brand-400 to-cyan-400 text-sm font-extrabold text-white">
              {session.name.slice(0, 2).toUpperCase()}
            </div>
          </div>
        </header>
        <div className="p-6 lg:p-10"><Outlet /></div>
      </main>
    </div>
  )
}
