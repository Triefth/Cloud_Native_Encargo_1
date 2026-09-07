# Plataforma de Telemedicina para Clínicas Rurales (DSY1107 - Desarrollo Cloud Native I)

Este repositorio contiene la solución full-stack desacoplada para el **Caso de Estudio 2: Plataforma de Telemedicina para Clínicas Rurales** de la asignatura **Desarrollo Cloud Native I (DSY1107)**.

La plataforma está diseñada bajo principios de **arquitectura Cloud Native**, **microservicios desacoplados**, **tolerancia a fallos con resiliencia activa**, **autenticación basada en JWT con Microsoft Entra ID (Azure AD)** e **infraestructura como código (IaC) en AWS mediante Terraform**.

---

##  Arquitectura General del Sistema

```
[ Usuario / Navegador Web ]
          │
          ├─────────────────────────────────────────────────┐
          ▼                                                 ▼
[ EC2 Frontend (SPA React 18) ]               [ AWS API Gateway (HTTP API) ]
  • Elastic IP Fija                             • JWT Authorizer (Microsoft Entra ID)
  • Nginx / Vite SPA Container                  • CORS Configurado
  • Login Landing Page (Entra ID + Demo)                    │
          │                                                 │ Proxy HTTP:8080
          └────────────────────────┐                        │
                                   ▼                        ▼
                       [ EC2 Backend (BFF Gateway: 8080) ]
                         • Resource Server Spring Security
                         • Circuit Breaker & Resiliencia (503 Handlers)
                                   │
       ┌──────────┬──────────┬─────┴────┬──────────┬──────────┬──────────┐
       ▼          ▼          ▼          ▼          ▼          ▼          ▼
   [Citas]  [Consultas]  [Fichas]  [Notific.] [Reportes] [Usuarios] [Clínicas]
   (8081)     (8082)     (8083)     (8084)     (8085)     (8086)     (8087)
```

---

##  Componentes del Proyecto

### 1. Frontend Web (React 18 + Vite)
Ubicación: `frontend/`
- **Diseño Glassmorphism Moderno:** Paleta de colores oscuros, efectos de brillo, tipografía Google Fonts (`Inter`, `Outfit`) y animaciones fluidas sin depender de frameworks pesados de utilidades (CSS Vanilla).
- **Pantalla de Login Previa (`LoginPage`):**
  - **Microsoft Entra ID (MSAL Browser SDK):** Autenticación empresarial real con OAuth2/OIDC.
  - **Acceso Rápido Demo:** Ingreso de un solo clic con roles predefinidos (**Médico Rural**, **Paciente**, **Administrador**) y generación de tokens JWT de prueba para agilizar la evaluación.
  - **Monitoreo de Estado:** Indicador en tiempo real del estado de conexión con el `bff-service` (Puerto 8080).
- **9 Módulos Interactivos:**
  1. **Resiliencia & Salud (`ServicesHealthMonitor`):** Estado en vivo de los 8 microservicios y simulación de caídas/Circuit Breakers.
  2. **Dev JWT MSAL (`JwtManager`):** Inspección de claims del token Bearer, firmas HMAC/RSA y validación con el BFF.
  3. **Agenda Citas (`CitasManager`):** Creación, confirmación, reprogramación y cancelación de citas médicas.
  4. **Teleconsulta CPaaS (`ConsultasManager`):** Simulación de sala de videollamadas con controles de cámara, micrófono, chat y emisión de recetas.
  5. **Fichas Médicas (`FichasManager`):** Historial clínico del paciente y registro de atenciones con integración EHR.
  6. **Pacientes & Médicos (`UsuariosManager`):** Maestro de usuarios y catálogo por especialidad médica.
  7. **Notificaciones (`NotificacionesManager`):** Envío de SMS, WhatsApp y correos electrónicos anti no-show.
  8. **Clínicas Rurales (`ClinicasManager`):** Catálogo de clínicas afiliadas y configuración de conectores EHR externos.
  9. **Reportes Operativos (`ReportesDashboard`):** Métricas operativas, gráficos de disponibilidad y auditoría de eventos.

---

### 2. Backend en Microservicios (Java 17 / Spring Boot 3)
Ubicación: `backend/`

Consta de **8 Microservicios independientes** con arquitectura desacoplada:

| Microservicio | Puerto | Descripción y Responsabilidad |
|---|---|---|
| `bff-service` | `8080` | **Backend for Frontend (BFF)**: Resource Server Spring Security con validación JWKS de Microsoft Entra, enrutamiento a microservicios y manejo controlado de fallas (`503 Service Unavailable`). |
| `citas-service` | `8081` | **Agenda de Citas**: Programación, confirmación, cancelación y reprogramación de atenciones médicas. |
| `consultas-service` | `8082` | **Consultas en Línea**: Gestión de sesiones de teleconsulta CPaaS, toma de datos médicos y diagnóstico preliminar. |
| `fichas-service` | `8083` | **Integración de Fichas Médicas**: Historial de atenciones del paciente e integración con el software de ficha clínica externo. |
| `notificaciones-service` | `8084` | **Notificaciones y Recordatorios**: Envío de avisos automatizados por SMS, WhatsApp y Email anti no-show. |
| `reportes-service` | `8085` | **Informes Operativos**: Registro auditado de eventos del sistema, métricas de latencia y disponibilidad. |
| `usuarios-service` | `8086` | **Maestro de Usuarios**: Fuente única de verdad para Pacientes, Médicos Voluntarios y Especialidades médicas. |
| `clinicas-service` | `8087` | **Gestor de Clínicas Rurales**: Catálogo de centros de salud afiliados y configuración de credenciales/endpoints de ficha clínica. |

---

### 3. Infraestructura como Código (AWS + Azure AD en Terraform)
Ubicación: `terraform/`

Terraform provisiona automáticamente la infraestructura desacoplada en la nube:

* **Instancia EC2 Backend (`Telemedicina-Backend-EC2`):**
  * Tipo `t2.medium` con disco expandido gp3 de 25 GB.
  * Elastic IP fija (`backend_eip`).
  * Security Group (`backend_sg`) con puertos `22` (SSH) y `8080` (BFF Gateway).
  * Automatización mediante `user_data` que instala Docker y Docker Compose para desplegar el backend completo.
* **Instancia EC2 Frontend (`Telemedicina-Frontend-EC2`):**
  * Instancia dedicada `t2.micro` con disco gp3 de 15 GB.
  * Elastic IP fija (`frontend_eip`).
  * Security Group (`frontend_sg`) con puertos `22` (SSH), `80` (HTTP), `443` (HTTPS) y `5173` (Vite SPA).
  * Automatización mediante `user_data` con Docker, Docker Compose y Nginx.
* **AWS API Gateway (HTTP API):**
  * HTTP Proxy hacia la IP fija del Backend en el puerto 8080.
  * **JWT Authorizer** integrado con Microsoft Entra ID.
  * Reglas CORS configuradas para permitir peticiones desde la IP pública del Frontend y entornos locales.
* **Microsoft Entra ID (Azure AD Provider):**
  * Creación automática de la **Backend Application API** y la **Frontend SPA Registration**.
  * Scopes de permisos `read` y `write` con consentimiento delegado pre-otorgado.

---

##  Autenticación y Seguridad JWT

1. **Microsoft Entra ID (Azure AD):** Los tokens JWT emitidos por Entra contienen información del usuario, emisor (`iss`), audiencia (`aud`) y roles asignados.
2. **Doble Validación de Seguridad:**
   - **Capa 1 (API Gateway):** AWS API Gateway valida la firma del token de manera nativa mediante el Authorizer JWT.
   - **Capa 2 (BFF Gateway):** El `bff-service` re-valida la firma con la clave pública JWKS de Microsoft Entra y autoriza las peticiones hacia los microservicios internos.
3. **Modo Demo para Evaluación:** El frontend incluye tokens JWT estructurados para pruebas sin necesidad de configurar credenciales de Azure en fases de evaluación local.

---

##  Instrucciones de Ejecución

### 1. Ejecutar el Backend (Docker Compose)
Navegar a la carpeta `backend` e iniciar los contenedores:
```bash
cd backend
docker compose up --build
```
*El BFF Gateway quedará disponible en `http://localhost:8080` y los 7 microservicios en sus respectivos puertos (8081 - 8087).*

### 2. Ejecutar el Frontend Web
Navegar a la carpeta `frontend`:
```bash
cd frontend
npm install
npm run dev
```
*La aplicación web estará disponible en `http://localhost:5173`. Mostrará primero la **Pantalla de Login** y luego dará paso a la plataforma.*

### 3. Aprovisionar Infraestructura con Terraform
Navegar a la carpeta `terraform`:
```bash
cd terraform
terraform init
terraform plan -var="azure_tenant_id=YOUR_TENANT_ID" -var="azure_issuer=YOUR_ISSUER_URI"
terraform apply -var="azure_tenant_id=YOUR_TENANT_ID" -var="azure_issuer=YOUR_ISSUER_URI"
```

---

##  Endpoints de la API Backend (`/api/bff/...`)

* **Autenticación & Estado:**
  * `GET /api/bff/auth/status` - Estado del BFF Gateway.
  * `GET /api/bff/auth/me` - Validación de token JWT y claims.
* **Agenda de Citas:** `GET /api/bff/citas`, `POST /api/bff/citas`, `PUT /api/bff/citas/{id}/confirmar`, `PUT /api/bff/citas/{id}/cancelar`, `PUT /api/bff/citas/{id}/reprogramar`
* **Consultas en Línea:** `POST /api/bff/consultas/iniciar`, `PUT /api/bff/consultas/{id}/finalizar`
* **Fichas Médicas:** `GET /api/bff/fichas/paciente/{rut}`, `POST /api/bff/fichas/paciente/{rut}/atencion`
* **Notificaciones:** `POST /api/bff/notificaciones/recordatorio`, `PUT /api/bff/notificaciones/{id}/lectura`
* **Usuarios:** `GET /api/bff/usuarios/pacientes`, `GET /api/bff/usuarios/medicos`, `GET /api/bff/usuarios/medicos/especialidad/{especialidad}`
* **Clínicas Rurales:** `GET /api/bff/clinicas`, `PUT /api/bff/clinicas/{id}/configuracion-ehr`
* **Reportes:** `GET /api/bff/reportes/resumen`, `GET /api/bff/reportes/modulo/{modulo}`
