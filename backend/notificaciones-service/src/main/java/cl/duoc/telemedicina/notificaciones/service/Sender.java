package cl.duoc.telemedicina.notificaciones.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Productor (Sender) - Envía mensajes a la cola 'hello' (Guía 1 Duoc UC).
 * Utiliza configuración centralizada vía application.yml.
 */
@Component
public class Sender {

    private static final Logger log = LoggerFactory.getLogger(Sender.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.queues.hello:hello}")
    private String helloQueue;

    public void sendMessage(String message) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            String fullMessage = String.format("[%s] %s", timestamp, message);

            rabbitTemplate.convertAndSend(helloQueue, fullMessage);
            log.info("[✓] Mensaje enviado a '{}': '{}'", helloQueue, fullMessage);
        } catch (Exception e) {
            log.error("[✗] Error enviando mensaje a '{}': {}", helloQueue, e.getMessage());
        }
    }

    public void sendMessage(String exchange, String routingKey, String message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("[✓] Mensaje enviado a exchange='{}', routingKey='{}': '{}'", exchange, routingKey, message);
        } catch (Exception e) {
            log.error("[✗] Error enviando mensaje a exchange '{}': {}", exchange, e.getMessage());
        }
    }
}
