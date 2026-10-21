package cl.duoc.telemedicina.notificaciones.service;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * =====================================================================
 * Dominio Funcional: Mensajería Directa Punto a Punto (Guía 1 Duoc UC)
 * Consumidor de la cola 'hello' con confirmación explícita (ACK/NACK).
 * =====================================================================
 */
@Component
public class Receiver {

    private static final Logger log = LoggerFactory.getLogger(Receiver.class);

    @RabbitListener(
            id = "hello-receiver-listener",
            queues = "${app.rabbitmq.queues.hello:hello}"
    )
    public void receiveMessage(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            log.info("[{}] [✓ MENSAJE RECIBIDO 'hello'] tag {}: '{}'", timestamp, deliveryTag, message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[✗ ERROR RECIBIENDO 'hello'] tag {}: {}", deliveryTag, e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
