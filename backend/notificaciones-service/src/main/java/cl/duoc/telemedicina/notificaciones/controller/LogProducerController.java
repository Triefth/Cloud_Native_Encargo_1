package cl.duoc.telemedicina.notificaciones.controller;

import cl.duoc.telemedicina.notificaciones.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // Permite peticiones desde Vite / Frontend
public class LogProducerController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/log")
    public ResponseEntity<Map<String, String>> sendLog(@RequestBody LogMessage logMessage) {
        // La routing key es el nivel del log (INFO, ERROR, etc.)
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                logMessage.getLevel(),
                logMessage.getMessage()
        );
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Log enviado: " + logMessage.getMessage(),
                "level", logMessage.getLevel()
        ));
    }

    @GetMapping("/log/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(Map.of("status", "ok", "message", "RabbitMQ LogProducerController está en línea."));
    }

    // DTO para el cuerpo de la petición
    public static class LogMessage {
        private String level;
        private String message;

        public LogMessage() {}

        public LogMessage(String level, String message) {
            this.level = level;
            this.message = message;
        }

        // Getters y Setters
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
