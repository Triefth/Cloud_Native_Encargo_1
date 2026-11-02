package com.telemedicina.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =====================================================================
 * CitaEventoDTO — Modelo de mensaje de ENTRADA al tópico
 * {@code telemedicina.citas.eventos} (formato JSON).
 *
 * Caso de uso EP3 #1: "Streaming del ciclo de vida de citas de telemedicina".
 * Producido por citas-service y consumido por kafka-service.
 *
 * Ejemplo JSON:
 * {
 *   "eventId": "9c7a...",
 *   "eventType": "CITA_CANCELADA",
 *   "citaId": 42,
 *   "rutPaciente": "12.345.678-9",
 *   "nombrePaciente": "María González",
 *   "rutMedico": "11.111.111-1",
 *   "nombreMedico": "Dr. Pérez",
 *   "especialidad": "Medicina General",
 *   "estado": "CANCELADA",
 *   "fechaHora": "2026-11-05T10:30:00",
 *   "motivoConsulta": "Control de hipertensión",
 *   "clinicaRural": "Clínica Rural San Pedro",
 *   "timestamp": "2026-11-02T17:00:00Z"
 * }
 * =====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaEventoDTO {

    public static final String EVENTO_CREADA = "CITA_CREADA";
    public static final String EVENTO_CONFIRMADA = "CITA_CONFIRMADA";
    public static final String EVENTO_CANCELADA = "CITA_CANCELADA";
    public static final String EVENTO_REPROGRAMADA = "CITA_REPROGRAMADA";

    /** Identificador único del evento (UUID) para trazabilidad e idempotencia. */
    @NotBlank(message = "eventId es obligatorio")
    @JsonProperty("eventId")
    private String eventId;

    /** Tipo de evento del ciclo de vida de la cita. */
    @NotBlank(message = "eventType es obligatorio")
    @JsonProperty("eventType")
    private String eventType;

    /** Identificador de la cita origen. */
    @NotNull(message = "citaId es obligatorio")
    @JsonProperty("citaId")
    private Long citaId;

    @NotBlank(message = "rutPaciente es obligatorio")
    @JsonProperty("rutPaciente")
    private String rutPaciente;

    @JsonProperty("nombrePaciente")
    private String nombrePaciente;

    @NotBlank(message = "rutMedico es obligatorio")
    @JsonProperty("rutMedico")
    private String rutMedico;

    @JsonProperty("nombreMedico")
    private String nombreMedico;

    @JsonProperty("especialidad")
    private String especialidad;

    @NotBlank(message = "estado es obligatorio")
    @JsonProperty("estado")
    private String estado;

    /** Fecha y hora de la cita en formato ISO-8601 (yyyy-MM-dd'T'HH:mm:ss). */
    @JsonProperty("fechaHora")
    private String fechaHora;

    @JsonProperty("motivoConsulta")
    private String motivoConsulta;

    @JsonProperty("clinicaRural")
    private String clinicaRural;

    /** Instante de emisión del evento en formato ISO-8601 (yyyy-MM-dd'T'HH:mm:ss'Z'). */
    @NotBlank(message = "timestamp es obligatorio")
    @JsonProperty("timestamp")
    private String timestamp;
}
