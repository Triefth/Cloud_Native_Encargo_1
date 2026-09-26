package cl.duoc.telemedicina.notificaciones.service;

import cl.duoc.telemedicina.notificaciones.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Consumidor (Receiver) - Recibe mensajes de la cola 'hello' (Guía 1)
 */
@Component
public class Receiver {

    @RabbitListener(queues = RabbitMQConfig.HELLO_QUEUE)
    public void receiveMessage(String message) {
        try {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));

            System.out.println(
                    "[" + timestamp + "] [✓] Mensaje recibido: '" + message + "'"
            );
        } catch (Exception e) {
            System.err.println("[✗] Error procesando mensaje: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
