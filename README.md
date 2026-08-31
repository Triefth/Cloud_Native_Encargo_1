# Backend Plataforma de Telemedicina para Clínicas Rurales (DSY1107 - Evaluación Parcial N°1)

Este proyecto corresponde a la arquitectura del backend en microservicios desacoplados para el **Caso de Estudio 2: Plataforma de Telemedicina para Clínicas Rurales** de la asignatura **Desarrollo Cloud Native I (DSY1107)**.

## Arquitectura del Backend

La solución consta de **8 Microservicios independientes** desarrollados en **Java 17 / Spring Boot 3**:

| Microservicio | Puerto | Descripción y Responsabilidad |
|---|---|---|
| `bff-service` | `8080` | **BFF / API Gateway**: Validación de tokens JWT (Azure AD / MSAL), emisor, audiencia, firma, expiración y roles. Enrutamiento resiliéte. |
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

## Autenticación y Validación JWT (Azure AD / MSAL)

El microservicio `bff-service` valida las peticiones mediante el filtro `JwtValidationFilter`:
- **Emisor (`iss`):** Verifica que el emisor corresponda al tenant de Azure AD.
- **Audiencia (`aud`):** Verifica que el token esté destinado a la API.
- **Vigencia (`exp`):** Comprueba que el token no haya expirado.
- **Firma & Claims:** Extrae el usuario y los roles (`roles`/`scp`) para autorizar la petición.

### Endpoint para Generar Token de Prueba (Modo Dev)
Para probar los endpoints protegidos sin necesidad de un tenant Azure AD activo, se proporciona un endpoint público de generación de tokens:

- **GET** `http://localhost:8080/api/bff/auth/dev-token?user=medico@rural.cl&role=MEDICO`

### Endpoint para Validar Token
- **POST** `http://localhost:8080/api/bff/auth/validate-token`
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
  ```

---

## Cómo Ejecutar el Proyecto

### Opción 1: Ejecutar cada Microservicio por separado (Maven)
En la carpeta de cada microservicio ejecutar:
```bash
./mvnw spring-boot:run
```
O con Maven instalado:
```bash
mvn spring-boot:run
```

### Opción 2: Levantar con Docker Compose
```bash
docker-compose up --build
```

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
