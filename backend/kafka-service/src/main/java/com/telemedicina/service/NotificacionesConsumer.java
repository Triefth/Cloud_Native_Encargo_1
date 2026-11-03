package com.telemedicina.service;

import com.telemedicina.dto.NotificacionEventoDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * NotificacionesConsumer — Consumidor Kafka en kafka-service para auditar
 * y almacenar en memoria los eventos emitidos en {@code telemedicina.notificaciones}.
 */
@Service
public class NotificacionesConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesConsumer.class);

    private final List<NotificacionEventoDTO> receivedNotificaciones = new CopyOnWriteArrayList<>();

    @KafkaListener(
            topics = "${app.kafka.topics.notificaciones:telemedicina.notificaciones}",
            groupId = "telemedicina-notificaciones-group",
            containerFactory = "telemedicinaNotificacionesKafkaListenerContainerFactory"
    )
    public void listenNotificacion(ConsumerRecord<String, NotificacionEventoDTO> record) {
        NotificacionEventoDTO notif = record.value();
        if (notif == null) {
            log.warn("[KAFKA - NOTIF CONSUMER] Record nulo ignorado en partición {}", record.partition());
            return;
        }

        log.info("📲 [KAFKA - NOTIF RECIBIDA] Notificación #{} [RUT: {} | Canal: {} | Partición: {} | Offset: {}]: {}",
                notif.getNotificacionId(), notif.getRutPaciente(), notif.getCanal(), record.partition(), record.offset(), notif.getMensaje());

        receivedNotificaciones.add(0, notif);
        if (receivedNotificaciones.size() > 50) {
            receivedNotificaciones.remove(receivedNotificaciones.size() - 1);
        }
    }

    public List<NotificacionEventoDTO> getReceivedNotificaciones() {
        return receivedNotificaciones;
    }

    public void clearNotificaciones() {
        receivedNotificaciones.clear();
    }
}

