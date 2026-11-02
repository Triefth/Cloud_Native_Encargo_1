package com.telemedicina.service;

import com.telemedicina.dto.CitaEventoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * CitasEventProducer — Productor Kafka del tópico {@code telemedicina.citas.eventos}.
 *
 * Se usa desde el controlador de simulación/demo para inyectar eventos de citas
 * y verificar el flujo completo (citas -> alertas) desde el frontend.
 * En producción, el productor real reside en citas-service.
 */
@Service
public class CitasEventProducer {

    private static final Logger log = LoggerFactory.getLogger(CitasEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.citas-eventos:telemedicina.citas.eventos}")
    private String topicCitasEventos;

    @Autowired
    public CitasEventProducer(@Qualifier("telemedicinaKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /** Publica un evento de cita usando el RUT del paciente como clave de partición. */
    public void publicarEvento(CitaEventoDTO evento) {
        log.info("[KAFKA PRODUCTOR CITAS] Publicando evento tipo='{}' citaId='{}' -> topic='{}'",
                evento.getEventType(), evento.getCitaId(), topicCitasEventos);
        kafkaTemplate.send(topicCitasEventos, evento.getRutPaciente(), evento);
        log.info("[✓] Evento publicado exitosamente: eventId='{}'", evento.getEventId());
    }
}
