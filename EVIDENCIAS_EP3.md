# Evidencias — Evaluación Parcial N°3 (EP3) · DSY1107 Desarrollo Cloud Native I

**Caso de estudio:** Plataforma de Telemedicina para Clínicas Rurales
**Tecnología de streaming:** Apache Kafka (complementando RabbitMQ existente)
**Ponderación:** 12% · Entrega grupal

---

## 1. Casos de uso implementados con Kafka

Se definieron **dos casos de uso de negocio**, uno por cada tópico, que **complementan** (no reemplazan) las integraciones previas con RabbitMQ.

### Caso de uso 1 — Streaming del ciclo de vida de citas

| Elemento | Detalle |
|---|---|
| Tópico | `telemedicina.citas.eventos` |
| Productor | `citas-service` (publica `CitaEventoDTO`) |
| Consumidor | `kafka-service` — `CitasEventConsumer` (@KafkaListener) |
| Consumer group | `telemedicina-citas-group` |
| Particiones / réplicas | 3 / 1 |
| Descripción | Cada vez que una cita se **crea, confirma, cancela o reprograma**, `citas-service` publica un evento JSON. El consumidor valida el payload, audita el evento y detecta condiciones que generan alertas. |

### Caso de uso 2 — Alertas clínicas en tiempo real

| Elemento | Detalle |
|---|---|
| Tópico | `telemedicina.alertas` |
| Productor | `kafka-service` — `AlertaProducer` (publica `AlertaClinicaDTO`) |
| Consumidor | `kafka-service` — `AlertasConsumer` (@KafkaListener) |
| Consumer group | `telemedicina-alertas-group` |
| Particiones / réplicas | 3 / 1 |
| Descripción | Cuando se detecta una **cita cancelada** (severidad ALTA) o **reprogramada** (severidad MEDIA), se genera una alerta clínica. El consumidor de alertas las recibe para su despacho (SMS/email) y auditoría. |

**Flujo completo:**

```
citas-service ──CitaEventoDTO──▶ [telemedicina.citas.eventos] ──▶ CitasEventConsumer
                                                                    │ (detecta cancelación/reprogramación)
                                                                    ▼
              AlertasConsumer ◀── [telemedicina.alertas] ◀── AlertaProducer (AlertaClinicaDTO)
```

---

## 2. Modelo de mensajes (entrada / salida) — formato JSON

### 2.1 Tópico `telemedicina.citas.eventos` — Entrada `CitaEventoDTO`

```json
{
  "eventId": "9c7a2f1e-4b3d-4a1e-9f0e-1a2b3c4d5e6f",
  "eventType": "CITA_CANCELADA",
  "citaId": 42,
  "rutPaciente": "12.345.678-9",
  "nombrePaciente": "María González",
  "rutMedico": "11.111.111-1",
  "nombreMedico": "Dr. Pérez",
  "especialidad": "Medicina General",
  "estado": "CANCELADA",
  "fechaHora": "2026-11-05T10:30:00",
  "motivoConsulta": "Control de hipertensión",
  "clinicaRural": "Clínica Rural San Pedro",
  "timestamp": "2026-11-02T17:00:00Z"
}
```

**Valores de `eventType`:** `CITA_CREADA`, `CITA_CONFIRMADA`, `CITA_CANCELADA`, `CITA_REPROGRAMADA`.

### 2.2 Tópico `telemedicina.alertas` — Salida `AlertaClinicaDTO`

```json
{
  "alertaId": "a1b2c3d4-5e6f-4a1e-9f0e-1a2b3c4d5e6f",
  "tipo": "CITA_CANCELADA",
  "severidad": "ALTA",
  "rutPaciente": "12.345.678-9",
  "nombrePaciente": "María González",
  "citaId": 42,
  "mensaje": "Cita cancelada para paciente 12.345.678-9 - se requiere reagendar.",
  "sourceEventId": "9c7a2f1e-4b3d-4a1e-9f0e-1a2b3c4d5e6f",
  "timestamp": "2026-11-02T17:00:01Z"
}
```

**Valores de `tipo`:** `CITA_CANCELADA`, `REPROGRAMACION`, `RIESGO_NO_SHOW`.
**Valores de `severidad`:** `ALTA`, `MEDIA`, `BAJA`.

> Ambos modelos usan **DTOs tipados con Lombok + validación JSR-380** (`@NotBlank`, `@NotNull`) y serialización JSON (Jackson `JsonSerializer`/`JsonDeserializer` sin cabeceras de tipo, para mantener el contrato portable entre servicios).

---

## 3. Docker Compose (RabbitMQ + Kafka + Kafka UI)

El archivo `backend/docker-compose.yml` levanta la infraestructura completa:

| Servicio | Puerto(s) | Descripción |
|---|---|---|
| `zookeeper` | 2181 | Coordinación del clúster Kafka (Confluent 7.5.0) |
| `kafka` | 9092 (host) / 29092 (interno) | Broker Kafka con retención configurada |
| `kafka-ui` | 8090 | Dashboard de monitoreo de tópicos, particiones, offsets y consumer groups |
| `rabbitmq-1` | 5672 / 15672 | Nodo 1 del clúster RabbitMQ (admin / admin123) |
| `rabbitmq-2` | 5673 / 15673 | Nodo 2 del clúster RabbitMQ |
| `citas-service` | 8081 | Microservicio de citas (productor Kafka + RabbitMQ) |
| `kafka-service` | 8888 | Consumidor/productor Kafka de telemedicina + administrador |
| `bff-service` + 7 microservicios | 8080–8087 | Backend completo |

**Cambios clave de EP3 en el compose:** `citas-service` recibe `SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092` y depende de `kafka`.

```bash
cd backend
docker compose up -d --build
```

---

## 4. Microservicios Kafka

### 4.1 `citas-service` — Productor

- `dto/CitaEventoDTO.java` — modelo de mensaje.
- `config/KafkaProducerConfig.java` — `KafkaTemplate<String, CitaEventoDTO>` con `JsonSerializer`, idempotencia (`acks=all`, `retries=3`).
- `service/CitaEventoKafkaProducer.java` — publica el evento con **clave = `rutPaciente`** (Sticky Partitioning: todas las citas de un paciente van a la misma partición).
- `service/CitaService.java` — dispara el evento en `crearCita`, `confirmarCita`, `cancelarCita` y `reprogramarCita`.

### 4.2 `kafka-service` — Consumidor + Productor + Administrador

| Clase | Responsabilidad |
|---|---|
| `dto/CitaEventoDTO.java`, `dto/AlertaClinicaDTO.java` | Modelos de mensajes JSON |
| `config/TelemedicinaKafkaConfig.java` | Cliente Kafka completo: productor JSON, consumer factories con `ErrorHandlingDeserializer`, `DefaultErrorHandler` + DLT, `KafkaAdmin`, `AdminClient` y declaración de tópicos con particiones/réplicas/retención/cleanup policy |
| `service/CitasEventConsumer.java` | `@KafkaListener` (group `telemedicina-citas-group`): valida payload, audita y detecta alertas |
| `service/AlertaProducer.java` | Publica `AlertaClinicaDTO` en `telemedicina.alertas` (clave = rutPaciente) |
| `service/AlertasConsumer.java` | `@KafkaListener` (group `telemedicina-alertas-group`): consume alertas |
| `service/TelemedicinaAdminService.java` | CRUD de tópicos (crear/listar/describir/eliminar) y consumer groups vía `AdminClient` |
| `controller/TelemedicinaKafkaController.java` | API REST `/api/telemedicina/**` para simulación, consulta y administración |

### 4.3 Manejo de errores y Dead Letter Topic (DLT)

- `ErrorHandlingDeserializer` intercepta *poison pills* sin romper el consumidor.
- `DefaultErrorHandler` con **backoff exponencial** (1s → 2s → 4s, máx 6s).
- `DeadLetterPublishingRecoverer` deriva mensajes fallidos a `<topic>.DLT`:
  - `telemedicina.citas.eventos.DLT`
  - `telemedicina.alertas.DLT`
- Payloads inválidos (`IllegalArgumentException`) van **directo a DLT** sin reintentos inútiles.

### 4.4 Consumer groups y balanceo de carga

- `telemedicina-citas-group` → consume `telemedicina.citas.eventos` (3 particiones, `concurrency=3`).
- `telemedicina-alertas-group` → consume `telemedicina.alertas` (3 particiones, `concurrency=3`).
- Esto permite que varias instancias del consumidor **dividan particiones** y balanceen la carga.

### 4.5 Parámetros de administración de tópicos

| Tópico | Particiones | Réplicas | Retención | Cleanup policy |
|---|---|---|---|---|
| `telemedicina.citas.eventos` | 3 | 1 | 604800000 ms (7 días) | delete |
| `telemedicina.citas.eventos.DLT` | 3 | 1 | 604800000 ms (7 días) | delete |
| `telemedicina.alertas` | 3 | 1 | 604800000 ms (7 días) | delete |
| `telemedicina.alertas.DLT` | 3 | 1 | 604800000 ms (7 días) | delete |

---

## 5. Integración Frontend ⇄ Backend

- Nuevo módulo React: `frontend/src/components/KafkaTelemedicinaManager.jsx`.
- Cliente API: `frontend/src/services/api.js` → `telemedicinaKafkaApi`.
- Proxy BFF: `KafkaBffController` enruta `/api/bff/telemedicina/**` → `kafka-service:8888/api/telemedicina/**`.
- Pestaña **"Kafka Telemedicina"** en el Navbar, con dos vistas:
  - **Flujo Citas → Alertas:** simular eventos, ver eventos consumidos y alertas generadas.
  - **Administrar:** listar/crear/eliminar tópicos y ver consumer groups.

El frontend sigue autenticado con JWT (MSAL/Entra ID) y envía el token en cada petición (`Authorization: Bearer`).

---

## 6. Pruebas unitarias locales

```bash
# kafka-service (tests de consumidor + contrato JSON)
cd backend/kafka-service
mvn test

# citas-service (test del productor)
cd ../citas-service
mvn test
```

| Clase de test | Tests | Cubre |
|---|---|---|
| `CitasEventConsumerTest` | 6 | Detección de alertas (cancelada ALTA, reprogramada MEDIA, creada/confirmada sin alerta) |
| `CitaEventoDTOSerializationTest` | 4 | Contrato JSON de `CitaEventoDTO` y `AlertaClinicaDTO` (serialización/round-trip) |
| `CitaEventoKafkaProducerTest` | 3 | Productor: tópico, clave (rutPaciente) y construcción del DTO |

*(También se mantienen los tests existentes de kafka-service: `KafkaConsumerTest`, `ResilientConsumerTest`, `DeadLetterConsumerTest`, `SlowConsumerTest`.)*

---

## 7. Despliegue del proyecto en AWS (EC2)

La infraestructura se provisiona con Terraform (`terraform/`) y el despliegue con Docker Compose (`backend/docker-compose.yml`). Ver `DEPLOYMENT_GUIDE.md` para el detalle paso a paso.

**Resumen:**
1. `terraform apply` → 2 EC2 (Backend `t2.medium`, Frontend `t2.micro`) + Elastic IP + Security Groups.
2. Backend: `scp -r backend/` → `docker compose up -d --build` (levanta microservicios + RabbitMQ + Kafka + Kafka UI).
3. Frontend: `scp -r frontend/` → `docker compose up -d --build` (Nginx sirve la SPA en puerto 80).

| Componente | Instancia | Puerto |
|---|---|---|
| Frontend (Nginx/React) | Frontend EC2 | 80 |
| BFF Gateway | Backend EC2 | 8080 |
| Microservicios | Backend EC2 | 8081–8087, 8888 |
| RabbitMQ (2 nodos) | Backend EC2 | 5672/15672, 5673/15673 |
| Kafka Broker + ZooKeeper | Backend EC2 | 9092 / 2181 |
| Kafka UI | Backend EC2 | 8090 |

---

## 8. Pruebas de integración en AWS y Azure

### 8.1 Autenticación de usuarios
1. Abrir el frontend en `http://<FRONTEND_PUBLIC_IP>`.
2. Login con **Microsoft Entra ID** (MSAL) o **acceso demo**.
3. Verificar en la barra superior que aparece **"JWT Activo"**.

### 8.2 Consumo de microservicios protegidos desde el frontend usando token
```bash
curl -H "Authorization: Bearer <JWT>" http://<BACKEND_PUBLIC_IP>:8080/api/bff/auth/me
curl -H "Authorization: Bearer <JWT>" http://<BACKEND_PUBLIC_IP>:8080/api/bff/telemedicina/health
```
El BFF valida el JWT (Resource Server) antes de enrutar a los microservicios.

### 8.3 Monitoreo de colas RabbitMQ con dashboard
- Nodo 1: `http://<BACKEND_PUBLIC_IP>:15672` (admin / admin123)
- Nodo 2: `http://<BACKEND_PUBLIC_IP>:15673`
- Verificar colas `citas.queue`, `citas.dlq`, exchanges y bindings.

### 8.4 Monitoreo de topics y métricas con Kafka UI
- `http://<BACKEND_PUBLIC_IP>:8090`
- Verificar los tópicos `telemedicina.citas.eventos`, `telemedicina.alertas` y sus `.DLT`.
- En la pestaña **Consumers**, observar los groups `telemedicina-citas-group` y `telemedicina-alertas-group` con sus offsets.

**Prueba de flujo completo (end-to-end):**
```bash
# 1) Publicar una cita cancelada (dispara alerta)
curl -X POST http://<BACKEND_PUBLIC_IP>:8080/api/bff/telemedicina/eventos/simular \
  -H "Content-Type: application/json" \
  -d '{"eventType":"CITA_CANCELADA","citaId":1001,"rutPaciente":"12.345.678-9","nombrePaciente":"María González","estado":"CANCELADA"}'

# 2) Ver alertas generadas
curl http://<BACKEND_PUBLIC_IP>:8080/api/bff/telemedicina/alertas

# 3) Ver tópicos y consumer groups (admin)
curl http://<BACKEND_PUBLIC_IP>:8080/api/bff/telemedicina/admin/topics
curl http://<BACKEND_PUBLIC_IP>:8080/api/bff/telemedicina/admin/consumer-groups
```

---

## 9. Entrega del proyecto semestral

### Checklist de entrega

- [x] Código frontend y backend completo y modular.
- [x] Backend compila (Java 17 / Spring Boot 3.2.3) y responde pruebas.
- [x] Filtros de validación JWT en el BFF.
- [x] Frontend con flujo de login (IDaaS) y uso de JWT en llamadas.
- [x] `.gitignore` configurado (excluye `target/`, `node_modules/`, `.env`, claves `.pem`, tfstate, etc.).
- [x] Productores/consumidores RabbitMQ + DLQ.
- [x] Productores/consumidores Kafka + DLT.
- [x] Microservicio administrador de Kafka (CRUD de tópicos y consumer groups).
- [x] `docker-compose` para RabbitMQ, Kafka (ZooKeeper + broker) y Kafka UI.
- [ ] Enlaces públicos a repositorios GitHub (frontend y backend).

### Repositorios GitHub

| Componente | Enlace |
|---|---|
| Backend | `https://github.com/<usuario>/<repositorio-backend>` *(reemplazar)* |
| Frontend | `https://github.com/<usuario>/<repositorio-frontend>` *(reemplazar)* |

### Documento de evidencias

Este mismo archivo (`EVIDENCIAS_EP3.md`) + capturas de pantalla a adjuntar:

- [ ] Captura: Kafka UI mostrando los 4 tópicos (2 principales + 2 DLT).
- [ ] Captura: Kafka UI → Consumers mostrando los 2 consumer groups.
- [ ] Captura: consola de `kafka-service` mostrando `[KAFKA CONSUMIDOR CITAS] Evento recibido...` y `[✓] Alerta publicada...`.
- [ ] Captura: RabbitMQ dashboard (colas/exchanges).
- [ ] Captura: frontend → pestaña "Kafka Telemedicina" con eventos y alertas.
- [ ] Captura: `mvn test` con todos los tests en verde.
- [ ] Captura: EC2 en ejecución + `docker compose ps`.

### Presentación

Preparar una presentación que cubra: arquitectura, los 2 casos de uso, modelo de mensajes JSON, docker-compose, microservicios Kafka (productores/consumidores/admin), manejo de errores (DLT), consumer groups, despliegue AWS y pruebas de integración.
