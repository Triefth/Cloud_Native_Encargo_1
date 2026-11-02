package cl.duoc.telemedicina.notificaciones.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO para consumir eventos de citas emitidos en el tópico {@code telemedicina.citas.eventos}.
 * Permite a notificaciones-service despachar recordatorios anti no-show de forma reactiva.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaEventoDTO implements Serializable {

    public static final String EVENTO_CREADA = "CITA_CREADA";
    public static final String EVENTO_CONFIRMADA = "CITA_CONFIRMADA";
    public static final String EVENTO_CANCELADA = "CITA_CANCELADA";
    public static final String EVENTO_REPROGRAMADA = "CITA_REPROGRAMADA";

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("citaId")
    private Long citaId;

    @JsonProperty("rutPaciente")
    private String rutPaciente;

    @JsonProperty("nombrePaciente")
    private String nombrePaciente;

    @JsonProperty("rutMedico")
    private String rutMedico;

    @JsonProperty("nombreMedico")
    private String nombreMedico;

    @JsonProperty("especialidad")
    private String especialidad;

    @JsonProperty("estado")
    private String estado;

    @JsonProperty("fechaHora")
    private String fechaHora;

    @JsonProperty("motivoConsulta")
    private String motivoConsulta;

    @JsonProperty("clinicaRural")
    private String clinicaRural;

    @JsonProperty("timestamp")
    private String timestamp;
}
