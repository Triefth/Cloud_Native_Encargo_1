package cl.duoc.telemedicina.notificaciones.service;

import cl.duoc.telemedicina.notificaciones.dto.NotificacionEventoDTO;
import cl.duoc.telemedicina.notificaciones.entity.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Productor Kafka para notificaciones-service.
 * Publica cada recordatorio o aviso despachado en el tópico {@code telemedicina.notificaciones}.
 */
@Service
public class NotificacionKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionKafkaProducer.class);

    private final KafkaTemplate<String, NotificacionEventoDTO> kafkaTemplate;

    @Value("${app.kafka.topics.notificaciones:telemedicina.notificaciones}")
    private String topicNotificaciones;

    @Autowired
    public NotificacionKafkaProducer(KafkaTemplate<String, NotificacionEventoDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicarNotificacion(Notificacion notif, String tipoOrigen) {
        try {
            NotificacionEventoDTO dto = NotificacionEventoDTO.builder()
                    .notificacionId(notif.getId())
                    .citaId(notif.getCitaId())
                    .rutPaciente(notif.getRutPaciente())
                    .canal(notif.getTipo() != null ? notif.getTipo().name() : "SMS")
                    .mensaje(notif.getMensaje())
                    .estado(notif.getEstado() != null ? notif.getEstado().name() : "ENVIADO")
                    .tipoOrigen(tipoOrigen != null ? tipoOrigen : "MANUAL_WEB")
                    .timestamp(Instant.now().toString())
                    .build();

            log.info("📤 [KAFKA - NOTIFICACION PRODUCTOR] Publicando evento #{} [RUT: {} | Canal: {}] en topic '{}'",
                    notif.getId(), notif.getRutPaciente(), dto.getCanal(), topicNotificaciones);

            // Clave: rutPaciente para garantizar orden de notificaciones por paciente en la misma partición
            kafkaTemplate.send(topicNotificaciones, notif.getRutPaciente(), dto);
            log.info("✓ [KAFKA] Notificación enviada exitosamente a topic '{}'", topicNotificaciones);
        } catch (Exception e) {
            log.error("❌ [KAFKA] Error al publicar notificación en Kafka: {}", e.getMessage(), e);
        }
    }
}

