package cl.duoc.telemedicina.citas.service;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * =====================================================================
 * CitaFeedbackConsumer: Consumidor Asíncrono en citas-service
 * Cumple con Pauta Duoc UC EP2:
 *  - "Los microservicios deben incluir en su código productores y consumidores"
 *  - Manejo de confirmación manual explícita (ACK/NACK) y logs de auditoría
 * =====================================================================
 */
@Service
public class CitaFeedbackConsumer {

    private static final Logger log = LoggerFactory.getLogger(CitaFeedbackConsumer.class);

    @RabbitListener(
            id = "citas-feedback-listener",
            queues = "${app.rabbitmq.queues.citas-feedback:citas.feedback.queue}"
    )
    public void receiveFeedback(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("[CITAS-SERVICE CONSUMER] Feedback recibido de auditoría (tag {}): {}", deliveryTag, message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[CITAS-SERVICE CONSUMER] Error procesando feedback tag {}: {}", deliveryTag, e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
