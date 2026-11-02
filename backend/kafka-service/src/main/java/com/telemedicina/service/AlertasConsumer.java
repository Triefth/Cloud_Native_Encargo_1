package com.telemedicina.service;

import com.telemedicina.dto.AlertaClinicaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AlertasConsumer — Consumidor Kafka del tópico {@code telemedicina.alertas}
 * con {@code @KafkaListener} y consumer group {@code telemedicina-alertas-group}.
 *
 * Representa el microservicio que recibe alertas clínicas para su despacho
 * (notificaciones SMS/email) y las audita en memoria para el frontend.
 */
@Service
public class AlertasConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertasConsumer.class);

    private final List<AlertaClinicaDTO> receivedAlertas = new CopyOnWriteArrayList<>();

    @KafkaListener(
            topics = "${app.kafka.topics.alertas:telemedicina.alertas}",
            groupId = "telemedicina-alertas-group",
            containerFactory = "telemedicinaAlertasKafkaListenerContainerFactory"
    )
    public void listen(
            @Payload AlertaClinicaDTO alerta,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[KAFKA CONSUMIDOR ALERTAS] Alerta recibida tipo='{}' severidad='{}' rutPaciente='{}' partition={} offset={}",
                alerta.getTipo(), alerta.getSeveridad(), alerta.getRutPaciente(), partition, offset);

        receivedAlertas.add(0, alerta);
        if (receivedAlertas.size() > 100) {
            receivedAlertas.remove(receivedAlertas.size() - 1);
        }
    }

    public List<AlertaClinicaDTO> getReceivedAlertas() {
        return receivedAlertas;
    }

    public void clearAlertas() {
        receivedAlertas.clear();
    }
}
