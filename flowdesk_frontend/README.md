# FlowDesk Frontend

Frontend web de FlowDesk construido con React, TypeScript, Vite y Tailwind CSS.

## Desarrollo local

1. Ejecuta el backend Spring Boot en `http://localhost:8080`.
2. Desde esta carpeta ejecuta `npm install`.
3. Inicia Vite con `npm run dev`.
4. Abre `http://localhost:5173`.

Vite redirige `/api` y `/ws` hacia Spring Boot durante el desarrollo. Para producción copia `.env.example` como `.env.production` y configura la URL pública de la API cuando frontend y backend no compartan dominio.

## Estructura

- `src/features`: módulos funcionales como auth, dashboard, proyectos y reportes.
- `src/layouts`: estructuras visuales compartidas.
- `src/lib`: cliente HTTP y utilidades transversales.
- `src/stores`: estado global mínimo.
- `src/types`: contratos TypeScript alineados con los DTO de Spring.
