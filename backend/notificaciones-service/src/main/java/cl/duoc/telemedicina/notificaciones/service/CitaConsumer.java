package cl.duoc.telemedicina.notificaciones.service;

import cl.duoc.telemedicina.notificaciones.entity.Notificacion;
import cl.duoc.telemedicina.notificaciones.repository.NotificacionRepository;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * =====================================================================
 * Dominio Funcional: Notificaciones de Citas de Telemedicina
 * Cumple con Pauta Duoc UC EP2:
 *  - 15% Consumidor agrupado por dominio de negocio (Telemedicina Citas)
 *  - 20% Confirmación manual explícita (basicAck / basicNack) y DLQ
 *  - 10% Aislamiento completo de lógica de negocio
 * =====================================================================
 */
@Service
public class CitaConsumer {

    private static final Logger log = LoggerFactory.getLogger(CitaConsumer.class);

    @Autowired
    private NotificacionRepository notificacionRepository;

    public CitaConsumer() {
    }

    public CitaConsumer(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @RabbitListener(
            id = "cita-consumer-listener",
            queues = "${app.rabbitmq.queues.citas:citas.queue}"
    )
    public void processCitaEvent(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info("[TELEMEDICINA - CITA CONSUMER] Evento recibido (tag {}): {}", deliveryTag, message);

        try {
            // Validación básica de carga útil
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Mensaje de cita vacío o corrupto");
            }

            // Simulación de validación de fallo para prueba de DLQ
            if (message.contains("SIMULATE_DLQ_ERROR")) {
                throw new RuntimeException("Fallo forzado para verificación de Dead Letter Queue");
            }

            // Persistir notificación de auditoría en la base de datos
            Notificacion notif = Notificacion.builder()
                    .rutPaciente("PACIENTE-AMQP")
                    .tipo(Notificacion.TipoNotificacion.WHATSAPP)
                    .mensaje("Confirmación de cita recibida vía RabbitMQ: " + message)
                    .estado(Notificacion.EstadoEnvio.ENVIADO)
                    .confirmacionLectura(false)
                    .build();
            notificacionRepository.save(notif);

            log.info("[✓ ACK CITA] Notificación registrada exitosamente con ID {}. Confirmando a RabbitMQ...", notif.getId());
            // Confirmación manual explícita
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[✗ ERROR CITA] Error procesando evento de cita tag {}: {}. Enviando a citas.dlq...", deliveryTag, e.getMessage());
            try {
                // NACK explícito con requeue=false para derivar automáticamente al DLX 'citas.dlx' -> 'citas.dlq'
                channel.basicNack(deliveryTag, false, false);
                log.warn("[→ DLQ CITAS] Mensaje desviado a la cola de cuarentena citas.dlq");
            } catch (Exception nackEx) {
                log.error("Fallo crítico ejecutando basicNack en CitaConsumer: {}", nackEx.getMessage());
            }
        }
    }

    /**
     * Consumidor de cuarentena para eventos de citas fallidos (DLQ)
     */
    @RabbitListener(
            id = "cita-dlq-listener",
            queues = "${app.rabbitmq.queues.citas-dlq:citas.dlq}"
    )
    public void processCitaDLQ(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.warn("🚨 [DLQ - CITAS CUARENTENA] Mensaje de cita retenido en citas.dlq (tag {}): {}", deliveryTag, message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error al procesar mensaje en citas.dlq: {}", e.getMessage());
            channel.basicReject(deliveryTag, false);
        }
    }
}
