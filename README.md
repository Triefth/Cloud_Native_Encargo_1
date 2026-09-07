# Backend Plataforma de Telemedicina para Clínicas Rurales (DSY1107 - Evaluación Parcial N°1)

Este proyecto corresponde a la arquitectura del backend en microservicios desacoplados para el **Caso de Estudio 2: Plataforma de Telemedicina para Clínicas Rurales** de la asignatura **Desarrollo Cloud Native I (DSY1107)**.

## Arquitectura del Backend

La solución consta de **8 Microservicios independientes** desarrollados en **Java 17 / Spring Boot 3**:

| Microservicio | Puerto | Descripción y Responsabilidad |
|---|---|---|
| `bff-service` | `8080` | **BFF**: Resource Server Spring Security con validación JWKS de Microsoft Entra y enrutamiento resiliente. |
| `citas-service` | `8081` | **Agenda de Citas**: Programación, confirmación, cancelación y reprogramación de atenciones remotas. |
| `consultas-service` | `8082` | **Consultas en Línea**: Salas de videollamadas HIPAA CPaaS, registro de atención médica. |
| `fichas-service` | `8083` | **Integración de Fichas Médicas**: Sincronización de atenciones remotas con el software de gestión de la clínica rural. |
| `notificaciones-service` | `8084` | **Notificaciones y Recordatorios**: Envío de SMS/WhatsApp/Email anti no-show. |
| `reportes-service` | `8085` | **Informes Operativos**: Monitoreo de latencias, caídas y métricas del sistema. |
| `usuarios-service` | `8086` | **Maestro de Usuarios**: Fuente única de verdad para registro y consulta de Pacientes, Médicos Voluntarios y Especialidades. |
| `clinicas-service` | `8087` | **Gestor de Clínicas Rurales**: Catálogo de clínicas afiliadas y configuración de credenciales/endpoints para integración con software de ficha clínica externo. |

---

## Principio de Desacoplamiento

Cada microservicio cuenta con su propia aplicación Spring Boot, su propio puerto y su propia base de datos (H2 en memoria configurable a PostgreSQL/MySQL). 

**Tolerancia a fallos:** Si un microservicio se detiene o presenta fallas, los demás microservicios continúan operando normalmente sin verse afectados. El `bff-service` captura la indisponibilidad de cualquier microservicio individual y responde con un código de error `503 Service Unavailable` controlado, manteniendo la estabilidad del ecosistema.

---

## Autenticación y Validación JWT (Microsoft Entra / MSAL)

Terraform crea la aplicación de la API, la SPA, los scopes `read`/`write`, sus service principals y el consentimiento delegado. AWS API Gateway valida primero el JWT mediante un Authorizer nativo de HTTP API; el `bff-service` vuelve a validarlo como Resource Server Spring Security usando el `issuer-uri`, JWKS, audiencia y expiración reales.

La única operación fuera de Terraform es crear el Tenant de Entra. El principal usado por CI debe existir una sola vez y tener permisos suficientes para administrar App Registrations y consentimientos. No se guardan secretos en el repositorio.

Endpoints públicos y protegidos:
- **GET** `/api/bff/auth/status`: estado del BFF.
- **GET** `/api/bff/auth/me`: claims del usuario autenticado; requiere `Authorization: Bearer <access-token>`.
- Todas las rutas de negocio requieren un access token emitido para la API.

### Variables necesarias

En Terraform se deben proporcionar `azure_tenant_id` y `azure_issuer`. En la SPA se usan `VITE_AZURE_CLIENT_ID`, `VITE_AZURE_TENANT_ID`, `VITE_AZURE_API_SCOPE` y `VITE_AZURE_REDIRECT_URI`; para External ID/User Flow define también `VITE_AZURE_AUTHORITY` con la autoridad CIAM exacta. En CI, configura `AZUREAD_CLIENT_ID`, `AZUREAD_CLIENT_SECRET`, `AZURE_TENANT_ID`, `AZURE_ISSUER`, `AZURE_AUDIENCE` y `FRONTEND_REDIRECT_URI` como secretos.

---

## Cómo Ejecutar el Proyecto

### 1. Ejecutar el Backend (8 Microservicios + BFF Gateway)
Navegar a la carpeta `backend`:
```bash
cd backend
docker compose up --build
```
*O ejecutando Maven de forma individual en cada carpeta dentro de `backend/`.*

### 2. Ejecutar el Frontend Web (React + Vite)
Navegar a la carpeta `frontend`:
```bash
cd frontend
npm install
npm run dev
```
La aplicación se abrirá en `http://localhost:5173` y se conectará automáticamente con el BFF Gateway en `http://localhost:8080`.


---

## Endpoints Principales

### Citas (`/api/bff/citas`)
- `GET /api/bff/citas`: Listar todas las citas.
- `POST /api/bff/citas`: Crear nueva cita médica.
- `PUT /api/bff/citas/{id}/confirmar`: Confirmar cita.
- `PUT /api/bff/citas/{id}/cancelar`: Cancelar cita.

### Consultas en Línea (`/api/bff/consultas`)
- `POST /api/bff/consultas/iniciar`: Crear sala de videollamada HIPAA CPaaS.
- `PUT /api/bff/consultas/{id}/finalizar`: Finalizar atención y guardar diagnóstico.

### Fichas Médicas (`/api/bff/fichas`)
- `GET /api/bff/fichas/paciente/{rut}`: Obtener ficha clínica e historial de atenciones remotas.
- `POST /api/bff/fichas/paciente/{rut}/atencion`: Registrar atención en la ficha de la clínica.

### Notificaciones (`/api/bff/notificaciones`)
- `POST /api/bff/notificaciones/recordatorio`: Enviar recordatorio anti no-show.

### Reportes (`/api/bff/reportes`)
- `GET /api/bff/reportes/resumen`: Ver métricas globales, disponibilidad y latencia.

### Usuarios (`/api/bff/usuarios`)
- `GET /api/bff/usuarios/pacientes`: Catálogo maestro de pacientes.
- `GET /api/bff/usuarios/medicos`: Catálogo maestro de médicos voluntarios.
- `GET /api/bff/usuarios/medicos/especialidad/{especialidad}`: Médicos por especialidad.

### Clínicas Rurales (`/api/bff/clinicas`)
- `GET /api/bff/clinicas`: Catálogo de clínicas rurales afiliadas.
- `PUT /api/bff/clinicas/{id}/configuracion-ehr`: Actualizar credenciales y endpoint API de la ficha clínica de la clínica rural.
