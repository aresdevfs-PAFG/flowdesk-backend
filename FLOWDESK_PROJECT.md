# FlowDesk — Documentación del proyecto

---

## ¿Qué es FlowDesk? (versión simple)

FlowDesk es una aplicación web donde equipos pequeños y freelancers pueden **organizar su trabajo y cobrar por él desde el mismo lugar**.

Hoy en día, quien trabaja por proyecto usa una herramienta para las tareas (Trello, Notion), otra para medir el tiempo (Toggl, Clockify) y otra para facturar (Excel, una app de facturación). FlowDesk une los tres en uno: gestionas las tareas, registras cuánto tiempo le dedicaste a cada una, y al final del mes generas la factura con el total calculado automáticamente.

**En una frase:** FlowDesk es el lugar donde haces el trabajo y donde cobras por él, sin saltar entre aplicaciones.

---

## Misión

Simplificar la gestión de proyectos y la facturación para equipos pequeños y profesionales independientes, eliminando la fricción de usar múltiples herramientas desconectadas entre sí.

---

## Visión

Ser la plataforma de referencia para freelancers y equipos de hasta 20 personas que necesitan una solución integrada, moderna y asequible para gestionar proyectos, medir productividad y cobrar a sus clientes — sin la complejidad ni el costo de herramientas enterprise como Jira o Monday.

---

## El problema que resuelve

| Problema real | Cómo lo resuelve FlowDesk |
|---|---|
| Gestionar tareas en Trello, tiempo en Toggl y facturas en Excel por separado | Todo en una sola plataforma integrada |
| No saber cuánto tiempo real se invirtió en un proyecto | Registro de tiempo por tarea con timer integrado |
| Calcular manualmente cuánto cobrar al cliente | La factura se genera automáticamente desde las horas registradas × tarifa del proyecto |
| Clientes que piden reportes de avance | Vista de progreso por proyecto exportable |
| Perder candidatos o tareas por falta de seguimiento | Kanban visual con estados, prioridades y fechas límite |

---

## A quién va dirigido

**Mercado primario:**
- Freelancers de diseño, desarrollo, consultoría o marketing
- Agencias pequeñas de 2 a 15 personas
- Estudios creativos o de software

**Mercado secundario:**
- Equipos internos de empresas medianas que necesitan visibilidad de proyectos sin implementar Jira
- Coordinadores de proyectos que hoy trabajan con hojas de cálculo

---

## Funcionalidades principales

### Gestión de proyectos
- Workspaces por organización o cliente
- Proyectos con fecha de inicio, fin y tarifa por hora
- Miembros con roles diferenciados: Manager, Collaborador, Viewer

### Tablero Kanban
- Tareas con estados: Por hacer, En progreso, En revisión, Completado, Cancelado
- Prioridades: Baja, Media, Alta, Crítica
- Asignación de responsables y fechas límite
- Reordenamiento por arrastre (drag and drop)

### Registro de tiempo
- Timer por tarea: inicia y detiene con un clic
- Registro manual de horas con nota
- Historial de tiempo por tarea, proyecto y usuario

### Facturación integrada
- Generación de factura desde las horas registradas en el proyecto
- Cálculo automático: horas × tarifa por hora del proyecto
- Estados de factura: Borrador, Enviada, Pagada, Vencida, Cancelada
- Exportación a PDF lista para enviar al cliente

### Comentarios en tiempo real
- Comentarios por tarea con actualización instantánea vía WebSockets
- Sin necesidad de recargar la página

### Notificaciones
- Alertas en tiempo real: tarea asignada, comentario nuevo, fecha límite próxima
- Persistencia en base de datos, contador de no leídas y marcado individual o masivo
- Canal privado STOMP `/user/queue/notifications`, autenticado con JWT durante `CONNECT`
- Recordatorios automáticos para tareas que vencen hoy o al día siguiente

### Reportes
- Horas invertidas por proyecto y por usuario
- Tareas completadas vs pendientes
- Resumen económico calculado con la tarifa horaria del proyecto
- Filtro opcional por período y exportación CSV

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 21 + Spring Boot 3.3.5 |
| Seguridad | Spring Security + JWT (jjwt 0.12.6) |
| Base de datos | PostgreSQL 15+ |
| ORM | Spring Data JPA + Hibernate |
| Migraciones | Flyway |
| Tiempo real | WebSockets (STOMP) |
| Mapeo de objetos | MapStruct 1.6.3 |
| Frontend | React 19 + Tailwind CSS |
| Build tool | Maven |
| Control de versiones | Git + GitHub |

---

## Arquitectura del sistema

```
Frontend (React 18)
       ↓ HTTP / WebSocket
API Gateway — Spring Security + JWT Filter
       ↓
Módulos de negocio (Spring Boot)
  ├── Auth          → registro, login, JWT
  ├── Workspaces    → organización por cliente o empresa
  ├── Projects      → proyectos con tarifa y fechas
  ├── Tasks         → kanban, estados, prioridades
  ├── Time Tracking → timer y registro manual de horas
  ├── Comments      → comentarios en tiempo real
  ├── Notifications → alertas por WebSocket
  ├── Billing       → facturas y cálculo automático
  └── Reports       → exportación y estadísticas
       ↓
PostgreSQL (Flyway migrations)
```

---

## Modelo de datos — tablas principales

| Tabla | Descripción |
|---|---|
| `users` | Usuarios del sistema |
| `workspaces` | Espacios de trabajo por organización |
| `workspace_members` | Membresías de usuarios a workspaces |
| `projects` | Proyectos con tarifa horaria |
| `project_members` | Roles de usuarios por proyecto |
| `tasks` | Tareas con estado, prioridad y posición Kanban |
| `time_entries` | Registros de tiempo por tarea y usuario |
| `comments` | Comentarios por tarea |
| `invoices` | Facturas por proyecto |
| `invoice_items` | Líneas de detalle de cada factura |
| `notifications` | Alertas persistentes, estado de lectura y referencia al recurso relacionado |

---

## Objetivos del proyecto

### Objetivos de producto
- Ofrecer una alternativa liviana a Jira y Monday para equipos pequeños
- Integrar gestión de tareas y facturación en una sola herramienta
- Ser usable sin capacitación — curva de aprendizaje mínima

### Objetivos técnicos (alcance de desarrollo actual)
- Construir un backend REST robusto en Spring Boot con autenticación JWT
- Implementar comunicación en tiempo real con WebSockets
- Gestionar el esquema de base de datos con migraciones versionadas (Flyway)
- Desarrollar un frontend moderno en React con experiencia fluida

---

## Alcance del MVP (versión inicial)

Lo que está dentro del MVP:

- Registro e inicio de sesión con JWT
- Creación y gestión de workspaces y proyectos
- Tablero Kanban con tareas, estados y prioridades
- Registro de tiempo por tarea (timer + manual)
- Generación y exportación de facturas en PDF
- Comentarios en tiempo real por tarea
- Reportes básicos por proyecto

Lo que queda fuera del MVP (versiones futuras):

- Integración con pasarelas de pago (Stripe, PayPal)
- App móvil nativa
- Integraciones con GitHub, Slack o Google Calendar
- IA para estimación automática de tiempos
- Facturación fiscal con CFDI (México) o equivalentes regionales

---

## Vías de oportunidad y modelo de negocio

### Modelo SaaS por suscripción

| Plan | Precio estimado | Incluye |
|---|---|---|
| Free | $0/mes | 1 proyecto, 2 usuarios, sin facturación |
| Pro | $12/mes | Proyectos ilimitados, hasta 5 usuarios, facturación PDF |
| Team | $35/mes | Usuarios ilimitados, reportes avanzados, soporte prioritario |

### Vías de monetización adicionales

**Mercado latinoamericano:** Hay una brecha enorme entre herramientas gratuitas básicas (Trello) y herramientas enterprise caras (Jira, Monday). FlowDesk apunta exactamente a ese espacio intermedio con precios accesibles en mercados como México, Colombia y Argentina.

**Agencias y estudios:** Una agencia de diseño o desarrollo que hoy usa Trello + Toggl + factura manual tiene un ahorro real de tiempo con FlowDesk. El argumento de venta es directo: "una sola app en lugar de tres".

**White-label:** A mediano plazo, ofrecer la plataforma bajo marca propia para agencias que quieren darle una herramienta de seguimiento a sus clientes.

**Marketplace de plantillas:** Plantillas de proyectos predefinidas por industria (agencia de marketing, estudio de desarrollo, consultoría legal) que los usuarios pueden comprar o descargar.

---

## Roadmap de desarrollo

| Fase | Contenido | Estado |
|---|---|---|
| Fase 1 | Enums, entidades JPA, migraciones Flyway | ✅ Completada |
| Fase 2 | Spring Security + JWT, Auth endpoints | ✅ Completada  |
| Fase 3 | Workspaces, Projects, Tasks CRUD + Kanban | ✅ Completada |
| Fase 4 | Time Tracking + Comments WebSocket | ✅ Completada |
| Fase 5 | Billing + generación de facturas PDF | ✅ Completada |
| Fase 6 | Reports + Notifications | ✅ Completada |
| Fase 7 | Frontend React — base + Kanban | En progreso — base, auth (login/registro), dashboard, workspaces, proyectos y Kanban de tareas implementados |
| Fase 8 | Frontend React — Billing + Reports | Pendiente |
| Fase 9 | Testing + Swagger + Docker + Deploy | Pendiente |

---

## Contrato de Reports y Notifications

Todos los endpoints requieren `Authorization: Bearer <token>`.

### Reports

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/projects/{projectId}/reports/summary` | Resumen de horas, importe, tareas y distribución por usuario |
| `GET` | `/api/projects/{projectId}/reports/summary.csv` | Descarga el mismo resumen en CSV |

Ambos aceptan `fromDate` y `toDate` opcionales en formato `YYYY-MM-DD`. El período filtra los registros de tiempo; las métricas de tareas representan el estado actual del tablero. Solo los miembros del proyecto pueden consultar sus reportes.

### Notifications

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/notifications/me` | Lista las notificaciones del usuario; acepta `unreadOnly=true` |
| `GET` | `/api/notifications/unread-count` | Devuelve el contador de no leídas |
| `PATCH` | `/api/notifications/{notificationId}/read` | Marca una notificación propia como leída |
| `PATCH` | `/api/notifications/read-all` | Marca todas las notificaciones propias como leídas |

El cliente WebSocket se conecta a `/ws`, envía el JWT en el header STOMP `Authorization` y se suscribe a `/user/queue/notifications`. Las notificaciones se generan mediante eventos internos después de confirmar la transacción que originó la asignación o el comentario. El recordatorio de vencimiento se ejecuta diariamente a las 08:00 en `America/Mexico_City`; ambos valores se pueden cambiar con `app.notifications.due-date-cron` y `app.notifications.time-zone`.

---

## Autor y contexto

**Desarrollador:** Ares Wayne
**Tipo de proyecto:** Portfolio personal / producto SaaS en desarrollo
**Repositorio:** GitHub (privado durante desarrollo)
**Inicio:** Abril 2026

Este proyecto nació con el doble propósito de aprender Spring Boot y React en profundidad, y de construir algo con valor de negocio real que pueda presentarse como producto y como demostración técnica en procesos de selección.

---

*Documento generado en Abril 2026 y actualizado en Julio 2026 — versión viva, se actualiza con cada fase completada.*
