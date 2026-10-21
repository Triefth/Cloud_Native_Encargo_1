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
 * Dominio Funcional: Procesamiento de Órdenes (Guía 3 Duoc UC)
 * Demuestra:
 *  - Confirmación manual de mensajes (ACK / NACK explícito) (20%)
 *  - Redirección automática a Dead Letter Queue (DLQ) en caso de fallo
 *  - Logging estructurado de auditoría
 * =====================================================================
 */
@Service
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    /**
     * Consumidor principal: Recibe órdenes de la cola principal.
     * Simula fallos de negocio para verificar el desvío a DLQ.
     */
    @RabbitListener(
            id = "order-listener",
            queues = "${app.rabbitmq.queues.orders:orders.queue}",
            containerFactory = "orderListenerFactory"
    )
    public void processOrder(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info("[ORDEN RECIBIDA] Procesando payload tag {}: {}", deliveryTag, message);

        try {
            // Simulación de fallo aleatorio (50% probabilidad) para demostrar resiliencia DLX
            if (message.contains("FAIL") || Math.random() < 0.45) {
                throw new RuntimeException("Error simulado de negocio al procesar orden: " + message);
            }

            // Procesamiento exitoso: Confirmación manual explícita (ACK)
            log.info("[✓ ACK EXITOSO] Orden procesada correctamente (tag {}): {}", deliveryTag, message);
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[✗ ERROR PROCESAMIENTO] Falló orden tag {}: {}. Derivando a DLQ...", deliveryTag, e.getMessage());
            try {
                // NACK con requeue=false: RabbitMQ transfiere automáticamente el mensaje al Dead Letter Exchange
                channel.basicNack(deliveryTag, false, false);
                log.warn("[→ DLQ] Mensaje {} enviado a Dead Letter Queue con éxito", deliveryTag);
            } catch (Exception nackEx) {
                log.error("[FATAL] Error ejecutando basicNack para tag {}: {}", deliveryTag, nackEx.getMessage());
            }
        }
    }

    /**
     * Consumidor de Dead Letter Queue (DLQ):
     * Monitorea, audita y registra los mensajes en cuarentena para análisis post-mortem.
     */
    @RabbitListener(
            id = "order-dlq-listener",
            queues = "${app.rabbitmq.queues.orders-dlq:orders.dlq}"
    )
    public void processDLQ(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.warn("🚨 [DLQ - CUARENTENA] Mensaje retenido en orders.dlq (tag {}): {}", deliveryTag, message);
            // Confirmamos lectura desde DLQ para registrar en auditoría
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error al procesar mensaje en DLQ: {}", e.getMessage());
            channel.basicReject(deliveryTag, false);
        }
    }
}
