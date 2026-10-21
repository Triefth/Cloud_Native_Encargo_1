package cl.duoc.telemedicina.notificaciones.service;

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
 * Dominio Funcional: Monitoreo y Auditoría de Logs (Guía 2 Duoc UC)
 * Gestiona colas 'all_logs_queue' y 'errors_only_queue' vinculadas a 'logs_direct_exchange'.
 * Implementa confirmación manual (ACK/NACK).
 * =====================================================================
 */
@Service
public class LogConsumer {

    private static final Logger log = LoggerFactory.getLogger(LogConsumer.class);

    @RabbitListener(
            id = "all-logs-listener",
            queues = "${app.rabbitmq.queues.all-logs:all_logs_queue}"
    )
    public void receiveAllLogs(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("[MONITOR GENERAL] Log recibido (tag {}): {}", deliveryTag, message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[✗ ERROR MONITOR GENERAL] Fallo al procesar log tag {}: {}", deliveryTag, e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(
            id = "errors-only-listener",
            queues = "${app.rabbitmq.queues.errors-only:errors_only_queue}"
    )
    public void receiveErrorLogs(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.error("🚨 [ALERTA CRÍTICA - ERROR QUEUE] Error detectado (tag {}): {}", deliveryTag, message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[✗ ERROR ALERTA CRÍTICA] Fallo al procesar error tag {}: {}", deliveryTag, e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
