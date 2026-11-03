package cl.duoc.telemedicina.notificaciones.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * NotificacionEventoDTO — Modelo de evento publicado por notificaciones-service
 * en el tópico Kafka {@code telemedicina.notificaciones}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionEventoDTO implements Serializable {

    @JsonProperty("notificacionId")
    private Long notificacionId;

    @JsonProperty("citaId")
    private Long citaId;

    @JsonProperty("rutPaciente")
    private String rutPaciente;

    @JsonProperty("canal")
    private String canal; // SMS, WHATSAPP, EMAIL

    @JsonProperty("mensaje")
    private String mensaje;

    @JsonProperty("estado")
    private String estado; // ENVIADO, PENDIENTE, FALLIDO

    @JsonProperty("tipoOrigen")
    private String tipoOrigen; // MANUAL_WEB, ALERTA_KAFKA, RECORDATORIO_AUTO

    @JsonProperty("timestamp")
    private String timestamp;
}

