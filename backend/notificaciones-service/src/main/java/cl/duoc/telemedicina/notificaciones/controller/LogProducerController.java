package cl.duoc.telemedicina.notificaciones.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller para el envío de logs a RabbitMQ (Guía 2 Duoc UC).
 * Utiliza nombres centralizados inyectados desde application.yml (12% rúbrica).
 */
@RestController
@CrossOrigin(origins = "*")
public class LogProducerController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchanges.logs:logs_direct_exchange}")
    private String logsExchange;

    @PostMapping("/log")
    public ResponseEntity<Map<String, String>> sendLog(@RequestBody LogMessage logMessage) {
        String severity = (logMessage.getLevel() != null) ? logMessage.getLevel().toUpperCase() : "INFO";
        rabbitTemplate.convertAndSend(
                logsExchange,
                severity,
                logMessage.getMessage()
        );
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Log enviado: " + logMessage.getMessage(),
                "level", severity
        ));
    }

    @GetMapping("/log/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(Map.of("status", "ok", "message", "RabbitMQ LogProducerController está en línea."));
    }

    public static class LogMessage {
        private String level;
        private String message;

        public LogMessage() {}

        public LogMessage(String level, String message) {
            this.level = level;
            this.message = message;
        }

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
