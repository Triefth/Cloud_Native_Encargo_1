package com.telemedicina.service;

import com.telemedicina.dto.AlertaClinicaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * AlertaProducer — Productor Kafka del tópico {@code telemedicina.alertas}.
 *
 * Publica alertas clínicas usando el RUT del paciente como clave de partición,
 * garantizando orden secuencial por paciente (Sticky Partitioning).
 */
@Service
public class AlertaProducer {

    private static final Logger log = LoggerFactory.getLogger(AlertaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.alertas:telemedicina.alertas}")
    private String topicAlertas;

    @Autowired
    public AlertaProducer(@Qualifier("telemedicinaKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Envía una alerta clínica al tópico de alertas.
     * La clave (rutPaciente) garantiza que todas las alertas del mismo paciente
     * se alojen en la misma partición, preservando el orden de los eventos.
     */
    public void enviarAlerta(AlertaClinicaDTO alerta) {
        log.info("[KAFKA PRODUCTOR ALERTAS] Publicando alerta tipo='{}' severidad='{}' rutPaciente='{}' -> topic='{}'",
                alerta.getTipo(), alerta.getSeveridad(), alerta.getRutPaciente(), topicAlertas);
        kafkaTemplate.send(topicAlertas, alerta.getRutPaciente(), alerta);
        log.info("[✓] Alerta publicada exitosamente: alertaId='{}'", alerta.getAlertaId());
    }
}
