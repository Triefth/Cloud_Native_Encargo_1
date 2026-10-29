# Plataforma de Telemedicina para Clínicas Rurales (DSY1107 - Desarrollo Cloud Native I)

Este repositorio contiene la solución full-stack desacoplada para el **Caso de Estudio 2: Plataforma de Telemedicina para Clínicas Rurales** de la asignatura **Desarrollo Cloud Native I (DSY1107)**.

La plataforma está diseñada bajo principios de **arquitectura Cloud Native**, **microservicios desacoplados**, **tolerancia a fallos con resiliencia activa**, **autenticación basada en JWT con Microsoft Entra ID (Azure AD)** e **infraestructura como código (IaC) en AWS mediante Terraform**.

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
  8. **RabbitMQ Logging (`RabbitMQLogging`):** Enrutamiento AMQP con DirectExchange, Routing Keys y DLQ.
  9. **Kafka Streams & Pipeline (`KafkaStreamsManager`):** Particiones, offsets, Sticky Partitioning (Guía 2) y pipeline de detección de fraude en tiempo real (Guía 1).
  10. **Clínicas Rurales (`ClinicasManager`):** Catálogo de clínicas afiliadas y configuración de conectores EHR externos.
  11. **Reportes Operativos (`ReportesDashboard`):** Métricas operativas, gráficos de disponibilidad y auditoría de eventos.

---

### 2. Backend en Microservicios (Java 17 / Spring Boot 3)
Ubicación: `backend/`

Consta de **Microservicios independientes** con arquitectura desacoplada, mensajería AMQP y streaming de eventos:

| Microservicio / Componente | Puerto | Descripción y Responsabilidad |
|---|---|---|
| `bff-service` | `8080` | **Backend for Frontend (BFF)**: Resource Server Spring Security con validación JWKS de Microsoft Entra, enrutamiento a microservicios y manejo controlado de fallas (`503 Service Unavailable`). |
| `citas-service` | `8081` | **Agenda de Citas**: Programación, confirmación, cancelación y reprogramación de atenciones médicas. |
| `consultas-service` | `8082` | **Consultas en Línea**: Gestión de sesiones de teleconsulta CPaaS, toma de datos médicos y diagnóstico preliminar. |
| `fichas-service` | `8083` | **Integración de Fichas Médicas**: Historial de atenciones del paciente e integración con el software de ficha clínica externo. |
| `notificaciones-service` | `8084` | **Notificaciones y Recordatorios**: Envío de avisos automatizados por SMS, WhatsApp y Email anti no-show. |
| `reportes-service` | `8085` | **Informes Operativos**: Registro auditado de eventos del sistema, métricas de latencia y disponibilidad. |
| `usuarios-service` | `8086` | **Maestro de Usuarios**: Fuente única de verdad para Pacientes, Médicos Voluntarios y Especialidades médicas. |
| `clinicas-service` | `8087` | **Gestor de Clínicas Rurales**: Catálogo de centros de salud afiliados y configuración de credenciales/endpoints de ficha clínica. |
| `kafka-service` | `8888` | **Kafka & Event Streaming**: Topics con 3 particiones, Sticky Partitioning (`/api/kafka/send`) y pipeline de Kafka Streams para detección de transacciones sospechosas (`/api/banco/transaccion`). |
| `kafka-ui` | `8090` | **Kafka UI**: Dashboard web visual para inspeccionar topics (`tutorial-particiones`, `banco-alertas`), particiones, offsets y consumer groups. |
| `kafka` | `9092` | **Broker Apache Kafka**: Servidor de mensajería para eventos de alta concurrencia. |
| `zookeeper` | `2181` | **ZooKeeper**: Coordinación de metadatos del cluster de Kafka. |

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


---

## 🟠 Event Streaming con Apache Kafka (`kafka-service` - Puerto 8888)

El `kafka-service` implementa **4 guías completas de Kafka** de la asignatura, cubriendo desde particionamiento hasta resiliencia avanzada con Dead Letter Topics.

### Guía 1 – Pipeline Kafka Streams (Detección de Fraude en Tiempo Real)

**Endpoint:** `POST /api/banco/transaccion`

| Topic | Descripción |
|-------|-------------|
| `banco-transacciones` | Entrada: eventos crudos de transacciones (3 particiones) |
| `banco-alertas` | Salida: alertas de fraude si `monto > $10,000` |

**Topología Kafka Streams:**
```
banco-transacciones  →  [filter: monto > 10000]  →  [mapValues: enrich alerta]  →  banco-alertas
```

Ejemplo cURL:
```bash
# Transacción normal (sin alerta)
curl -X POST http://localhost:8080/api/banco/transaccion \
  -H "Content-Type: application/json" \
  -d '{"usuario": "juan", "monto": 500, "origen": "app"}'

# Transacción fraudulenta (dispara alerta en banco-alertas)
curl -X POST http://localhost:8080/api/banco/transaccion \
  -H "Content-Type: application/json" \
  -d '{"usuario": "juan", "monto": 15000, "origen": "cajero"}'
```

---

### Guía 2 – Particiones, Productor y Consumidor (Round Robin vs Sticky Partitioning)

**Endpoint:** `GET /api/kafka/send?message=<msg>[&key=<key>]`

| Comportamiento | Comando |
|---|---|
| **Round Robin** (sin key) | `GET /api/kafka/send?message=Mensaje1` |
| **Sticky Partitioning** (con key) | `GET /api/kafka/send?message=Paso1-Creada&key=orden-555` |

**Topic:** `tutorial-particiones` (3 particiones, replicación 1)

Con la misma `key`, Kafka calcula `hash(key) % 3` → todos los pasos de `orden-555` van **siempre a la misma partición**, garantizando **orden secuencial**.

---

### Guía 3 – Tuning, Pruebas de Carga & Consumer Lag (PDF 2 y PDF 3)

**Endpoint:** `POST /api/tuning/load?count=10000`

#### Configuración de Tuning del Productor (`KafkaTuningConfig`)

| Parámetro | Valor | Descripción |
|---|---|---|
| `batch.size` | 32 KB | Doble del default (16 KB) — menos llamadas I/O de red |
| `linger.ms` | 20 ms | Pausa para llenar buffers antes de enviar |
| `compression.type` | `snappy` | Balance óptimo CPU / compresión |
| `enable.idempotence` | `true` | Garantía exactly-once en el broker |
| `acks` | `all` | Confirmación en todas las réplicas |

#### Retención del Broker (docker-compose)

| Parámetro | Valor |
|---|---|
| `KAFKA_LOG_RETENTION_MINUTES` | 5 minutos |
| `KAFKA_LOG_RETENTION_BYTES` | 50 MB |
| `KAFKA_LOG_RETENTION_CHECK_INTERVAL_MS` | 60 segundos |

#### Fórmula Consumer Lag

```
Consumer Lag = LogEndOffset (total producidos) − CurrentOffset (total consumidos)
```

El `SlowConsumer` simula procesamiento pesado con retardo configurable (10 ms default = máx 100 msg/s).

---

### Guía 4 – Manejo de Errores, Resiliencia & Dead Letter Topic (PDF 1)

#### 5 Patrones de Resiliencia Implementados

| # | Patrón | Implementación |
|---|---|---|
| 1 | **Poison Pill** | `ErrorHandlingDeserializer` intercepta bytes corruptos sin bloquear la partición |
| 2 | **Validación de Payload** | JSR-380 (`@Min`, `@NotBlank`, `@Email`) en `TransaccionSegura` |
| 3 | **Exponential Backoff** | `DefaultErrorHandler` con espera 1s → 2s → 4s (máx 6s) antes del DLT |
| 4 | **Dead Letter Topic** | `DeadLetterPublishingRecoverer` → `banco-resiliente.DLT` con Header Enrichment |
| 5 | **Idempotencia Check-then-Act** | `processedMessages` en `ConcurrentHashMap` evita doble cobro por duplicados |

#### Endpoints de Simulación

```bash
POST /api/resilience/send                            # Transacción normal con Correlation ID
POST /api/resilience/simulate-poison-pill            # Bytes corruptos
POST /api/resilience/simulate-validation-error       # Monto negativo (falla @Min)
POST /api/resilience/simulate-duplicate?customId=X   # Duplicado (deduplicación)
POST /api/resilience/simulate-transient-error        # Backoff → recupera en intento 3
POST /api/resilience/simulate-permanent-error        # → banco-resiliente.DLT
GET  /api/resilience/dlt                             # Ver Sala de Emergencias
POST /api/resilience/dlt/reprocess/{id}              # Reprocesar mensaje del DLT
GET  /api/resilience/audit-logs                      # Auditoría MDC (Correlation IDs)
```

#### Topics Kafka

| Topic | Particiones | Guía |
|---|---|---|
| `tutorial-particiones` | 3 | Guía 2 |
| `banco-transacciones` | 3 | Guía 1 |
| `banco-alertas` | 3 | Guía 1 |
| `sensor-topic` | 3 | Guía 3 |
| `banco-resiliente` | 3 | Guía 4 |
| `banco-resiliente.DLT` | 3 | Guía 4 |

---

## 🧪 Tests Unitarios (`kafka-service`)

```bash
cd backend/kafka-service
# Con Maven instalado:
mvn test
# O dentro del contenedor Docker:
docker run --rm -v ./:/app -w /app maven:3.9-eclipse-temurin-17 mvn test
```

| Clase de Test | Tests | Cubre |
|---|---|---|
| `ResilientConsumerTest` | 10 | Idempotencia, modelos Lombok, limpieza de estado |
| `DeadLetterConsumerTest` | 9 | DLT vacío, operaciones CRUD, Serializable |
| `SlowConsumerTest` | 11 | Consumer Lag, pausa, retardo configurable, reset |
| `KafkaConsumerTest` | 7 | Particiones, Sticky Partitioning, Round Robin, limpieza |

---
