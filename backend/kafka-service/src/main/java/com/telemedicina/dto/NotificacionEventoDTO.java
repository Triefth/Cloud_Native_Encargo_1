package com.telemedicina.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * NotificacionEventoDTO — Modelo de evento consumido por kafka-service
 * desde el tópico {@code telemedicina.notificaciones}.
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
    private String canal;

    @JsonProperty("mensaje")
    private String mensaje;

    @JsonProperty("estado")
    private String estado;

    @JsonProperty("tipoOrigen")
    private String tipoOrigen;

    @JsonProperty("timestamp")
    private String timestamp;
}

