import { Navigate, Route, Routes } from 'react-router-dom'
import { LoginPage } from './features/auth/LoginPage'
import { ProtectedRoute } from './features/auth/ProtectedRoute'
import { DashboardPage } from './features/dashboard/DashboardPage'
import { AppShell } from './layouts/AppShell'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route index element={<DashboardPage />} />
          <Route path="projects" element={<Placeholder title="Proyectos" />} />
          <Route path="reports" element={<Placeholder title="Reportes" />} />
          <Route path="billing" element={<Placeholder title="Facturación" />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

function Placeholder({ title }: { title: string }) {
  return (
    <section className="surface-card p-8">
      <p className="eyebrow">Módulo preparado</p>
      <h1 className="mt-3 text-3xl font-semibold tracking-tight text-white">{title}</h1>
      <p className="mt-3 max-w-xl text-sm leading-6 text-slate-400">
        La ruta y la navegación ya están listas para conectar los endpoints de este módulo.
      </p>
    </section>
  )
}
