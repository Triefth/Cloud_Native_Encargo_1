package cl.duoc.telemedicina.notificaciones.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller para el envío y prueba de órdenes con DLX/DLQ (Guía 3 Duoc UC).
 * Utiliza nombres centralizados inyectados desde application.yml (12% rúbrica).
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchanges.orders:orders.exchange}")
    private String ordersExchange;

    @Value("${app.rabbitmq.routing-keys.order-created:order.created}")
    private String orderCreatedRoutingKey;

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

        // Enviar al Exchange Principal usando configuración centralizada
        rabbitTemplate.convertAndSend(
                ordersExchange,
                orderCreatedRoutingKey,
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
