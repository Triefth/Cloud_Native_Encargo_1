package com.telemedicina.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =====================================================================
 * AlertaClinicaDTO — Modelo de mensaje de SALIDA en el tópico
 * {@code telemedicina.alertas} (formato JSON).
 *
 * Caso de uso EP3 #2: "Alertas clínicas en tiempo real".
 * Producido por kafka-service (a partir de eventos de citas) y consumido
 * por kafka-service/notificaciones-service para su despacho.
 *
 * Ejemplo JSON:
 * {
 *   "alertaId": "a1b2...",
 *   "tipo": "CITA_CANCELADA",
 *   "severidad": "ALTA",
 *   "rutPaciente": "12.345.678-9",
 *   "nombrePaciente": "María González",
 *   "citaId": 42,
 *   "mensaje": "Cita cancelada, se requiere reagendar.",
 *   "sourceEventId": "9c7a...",
 *   "timestamp": "2026-11-02T17:00:01Z"
 * }
 * =====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaClinicaDTO {

    public static final String TIPO_CANCELADA = "CITA_CANCELADA";
    public static final String TIPO_REPROGRAMADA = "REPROGRAMACION";
    public static final String TIPO_RIESGO_NO_SHOW = "RIESGO_NO_SHOW";

    public static final String SEVERIDAD_ALTA = "ALTA";
    public static final String SEVERIDAD_MEDIA = "MEDIA";
    public static final String SEVERIDAD_BAJA = "BAJA";

    /** Identificador único de la alerta (UUID). */
    @NotBlank(message = "alertaId es obligatorio")
    @JsonProperty("alertaId")
    private String alertaId;

    /** Tipo de alerta clínica generada. */
    @NotBlank(message = "tipo es obligatorio")
    @JsonProperty("tipo")
    private String tipo;

    /** Severidad de la alerta: ALTA | MEDIA | BAJA. */
    @NotBlank(message = "severidad es obligatorio")
    @JsonProperty("severidad")
    private String severidad;

    @NotBlank(message = "rutPaciente es obligatorio")
    @JsonProperty("rutPaciente")
    private String rutPaciente;

    @JsonProperty("nombrePaciente")
    private String nombrePaciente;

    @JsonProperty("citaId")
    private Long citaId;

    @JsonProperty("mensaje")
    private String mensaje;

    /** eventId del evento que originó la alerta (trazabilidad). */
    @JsonProperty("sourceEventId")
    private String sourceEventId;

    @NotBlank(message = "timestamp es obligatorio")
    @JsonProperty("timestamp")
    private String timestamp;
}
