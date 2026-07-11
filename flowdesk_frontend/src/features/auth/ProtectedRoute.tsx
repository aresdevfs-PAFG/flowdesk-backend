import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '../../stores/auth-store'

export function ProtectedRoute() {
  const session = useAuthStore((state) => state.session)
  const location = useLocation()

  if (!session) return <Navigate to="/login" state={{ from: location }} replace />
  return <Outlet />
}
