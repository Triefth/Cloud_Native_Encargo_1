package cl.duoc.telemedicina.notificaciones.controller;

import cl.duoc.telemedicina.notificaciones.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendOrder(@RequestBody Map<String, String> payload) {
        String orderId = payload.getOrDefault("orderId", "ORD-" + System.currentTimeMillis());
        String customerName = payload.getOrDefault("customerName", "Cliente Anónimo");

        String message = String.format(
                "Orden ID: %s | Cliente: %s | Hora: %s",
                orderId,
                customerName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        );

        // Enviar al Exchange Principal
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDERS_EXCHANGE,
                RabbitMQConfig.ORDERS_ROUTING_KEY,
                message
        );

        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("orderId", orderId);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("backend", "Online");
        status.put("rabbitmq", "Conectado");
        status.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(status);
    }
}
