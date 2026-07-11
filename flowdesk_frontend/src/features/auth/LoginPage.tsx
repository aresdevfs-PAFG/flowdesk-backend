import { useState, type FormEvent } from 'react'
import { useMutation } from '@tanstack/react-query'
import { ArrowRight, Layers3, ShieldCheck, Sparkles } from 'lucide-react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { getApiError } from '../../lib/api'
import { useAuthStore } from '../../stores/auth-store'
import { login } from './auth-api'

export function LoginPage() {
  const session = useAuthStore((state) => state.session)
  const setSession = useAuthStore((state) => state.setSession)
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      setSession(data)
      const destination = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? '/'
      navigate(destination, { replace: true })
    },
  })

  if (session) return <Navigate to="/" replace />

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    mutation.mutate({ email, password })
  }

  return (
    <main className="grid min-h-screen lg:grid-cols-[1.08fr_0.92fr]">
      <section className="relative hidden overflow-hidden border-r border-white/8 p-12 lg:flex lg:flex-col lg:justify-between">
        <div className="absolute -left-28 top-12 h-96 w-96 rounded-full bg-brand-500/15 blur-3xl" />
        <Brand />
        <div className="relative max-w-xl">
          <p className="eyebrow">Trabajo claro. Negocio saludable.</p>
          <h1 className="mt-5 text-6xl font-semibold leading-[1.05] tracking-[-0.055em] text-white">
            Del tablero a la factura, sin perder el ritmo.
          </h1>
          <p className="mt-7 max-w-lg text-base leading-7 text-slate-400">
            Gestiona proyectos, registra cada minuto y convierte el trabajo de tu equipo en resultados medibles.
          </p>
        </div>
        <div className="relative flex gap-7 text-xs font-semibold uppercase tracking-[0.12em] text-slate-500">
          <span className="flex items-center gap-2"><ShieldCheck size={15} /> JWT seguro</span>
          <span className="flex items-center gap-2"><Sparkles size={15} /> Tiempo real</span>
        </div>
      </section>

      <section className="flex items-center justify-center px-6 py-12 sm:px-12">
        <div className="w-full max-w-md">
          <div className="mb-12 lg:hidden"><Brand /></div>
          <p className="eyebrow">Bienvenido de vuelta</p>
          <h2 className="mt-3 text-4xl font-semibold tracking-[-0.04em] text-white">Entra a tu espacio</h2>
          <p className="mt-3 text-sm leading-6 text-slate-400">Usa la cuenta registrada en el backend de FlowDesk.</p>

          <form className="mt-9 space-y-5" onSubmit={handleSubmit}>
            <label className="block">
              <span className="mb-2 block text-xs font-semibold text-slate-300">Correo electrónico</span>
              <input className="field" type="email" autoComplete="email" value={email}
                onChange={(event) => setEmail(event.target.value)} placeholder="tu@equipo.com" required />
            </label>
            <label className="block">
              <span className="mb-2 block text-xs font-semibold text-slate-300">Contraseña</span>
              <input className="field" type="password" autoComplete="current-password" value={password}
                onChange={(event) => setPassword(event.target.value)} placeholder="Mínimo 8 caracteres" required />
            </label>

            {mutation.isError && (
              <p className="rounded-xl border border-red-400/15 bg-red-400/8 px-4 py-3 text-sm text-red-300">
                {getApiError(mutation.error)}
              </p>
            )}

            <button className="flex w-full items-center justify-center gap-2 rounded-xl bg-brand-500 px-5 py-3.5 text-sm font-bold text-white transition hover:bg-brand-400 disabled:cursor-wait disabled:opacity-60"
              disabled={mutation.isPending}>
              {mutation.isPending ? 'Entrando…' : 'Entrar a FlowDesk'}
              {!mutation.isPending && <ArrowRight size={17} />}
            </button>
          </form>
        </div>
      </section>
    </main>
  )
}

function Brand() {
  return (
    <div className="relative flex items-center gap-3 text-lg font-bold tracking-tight text-white">
      <span className="grid h-10 w-10 place-items-center rounded-xl bg-brand-500 shadow-lg shadow-brand-500/20">
        <Layers3 size={20} />
      </span>
      FlowDesk
    </div>
  )
}
