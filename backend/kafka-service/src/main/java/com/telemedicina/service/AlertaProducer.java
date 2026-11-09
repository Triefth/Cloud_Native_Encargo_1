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
 * Publica alertas clínicas usando la especialidad médica como clave de partición,
 * garantizando orden secuencial por especialidad y balanceo en las 3 particiones.
 */
@Service
public class AlertaProducer {

    private static final Logger log = LoggerFactory.getLogger(AlertaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.alertas.name:${app.kafka.topics.alertas:telemedicina.alertas}}")
    private String topicAlertas;

    @Autowired
    public AlertaProducer(@Qualifier("telemedicinaKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Envía una alerta clínica al tópico de alertas.
     * La clave (especialidad médica) garantiza que todas las alertas de la misma especialidad
     * se alojen en la misma partición, preservando el orden de los eventos clínicos.
     */
    public void enviarAlerta(AlertaClinicaDTO alerta) {
        String partitionKey = (alerta.getEspecialidad() != null && !alerta.getEspecialidad().isBlank())
                ? alerta.getEspecialidad().trim()
                : "MEDICINA_GENERAL";

        log.info("[KAFKA PRODUCTOR ALERTAS] Publicando alerta tipo='{}' severidad='{}' key(especialidad)='{}' -> topic='{}'",
                alerta.getTipo(), alerta.getSeveridad(), partitionKey, topicAlertas);
        kafkaTemplate.send(topicAlertas, partitionKey, alerta);
        log.info("[✓] Alerta publicada exitosamente: alertaId='{}' key='{}'", alerta.getAlertaId(), partitionKey);
    }
}
