package cl.duoc.telemedicina.citas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CitaEventoDTO — Modelo de mensaje de SALIDA (JSON) que citas-service publica
 * en el tópico Kafka {@code telemedicina.citas.eventos}.
 *
 * Representa un evento del ciclo de vida de una cita:
 * CITA_CREADA | CITA_CONFIRMADA | CITA_CANCELADA | CITA_REPROGRAMADA.
 *
 * El contrato JSON es idéntico al consumido por kafka-service.
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

    @NotBlank(message = "eventId es obligatorio")
    @JsonProperty("eventId")
    private String eventId;

    @NotBlank(message = "eventType es obligatorio")
    @JsonProperty("eventType")
    private String eventType;

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

    @JsonProperty("fechaHora")
    private String fechaHora;

    @JsonProperty("motivoConsulta")
    private String motivoConsulta;

    @JsonProperty("clinicaRural")
    private String clinicaRural;

    @NotBlank(message = "timestamp es obligatorio")
    @JsonProperty("timestamp")
    private String timestamp;
}
