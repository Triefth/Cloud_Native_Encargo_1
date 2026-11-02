package cl.duoc.telemedicina.notificaciones.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO para consumir alertas clínicas emitidas en el tópico {@code telemedicina.alertas}.
 * Formato interoperable JSON (Caso de uso EP3: Alertas clínicas en tiempo real).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaClinicaDTO implements Serializable {

    public static final String TIPO_CANCELADA = "CITA_CANCELADA";
    public static final String TIPO_REPROGRAMADA = "REPROGRAMACION";
    public static final String TIPO_RIESGO_NO_SHOW = "RIESGO_NO_SHOW";

    public static final String SEVERIDAD_ALTA = "ALTA";
    public static final String SEVERIDAD_MEDIA = "MEDIA";
    public static final String SEVERIDAD_BAJA = "BAJA";

    @JsonProperty("alertaId")
    private String alertaId;

    @JsonProperty("tipo")
    private String tipo;

    @JsonProperty("severidad")
    private String severidad;

    @JsonProperty("rutPaciente")
    private String rutPaciente;

    @JsonProperty("nombrePaciente")
    private String nombrePaciente;

    @JsonProperty("citaId")
    private Long citaId;

    @JsonProperty("mensaje")
    private String mensaje;

    @JsonProperty("sourceEventId")
    private String sourceEventId;

    @JsonProperty("timestamp")
    private String timestamp;
}

