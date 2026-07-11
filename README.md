# FlowDesk

Repositorio principal de FlowDesk. Contiene el backend Spring Boot y el frontend web React.

## Estructura

- `src`: backend Java 21, Spring Boot, PostgreSQL, JWT y WebSockets.
- `flowdesk_frontend`: frontend React, TypeScript, Vite y Tailwind CSS.

## Ejecutar el backend

```powershell
.\mvnw.cmd spring-boot:run
```

El backend queda disponible en `http://localhost:8080`.

## Ejecutar el frontend

```powershell
cd flowdesk_frontend
npm install
npm run dev
```

El frontend queda disponible en `http://localhost:5173`. Durante desarrollo, Vite redirige las peticiones `/api` y `/ws` al backend.
