package cl.duoc.telemedicina.notificaciones.controller;

import cl.duoc.telemedicina.notificaciones.service.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller para enviar mensajes a RabbitMQ (Guía 1)
 */
@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private Sender sender;

    /**
     * Enviar un mensaje vía POST
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody MessageRequest request) {
        try {
            sender.sendMessage(request.getMessage());
            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "message", "Mensaje enviado: " + request.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Enviar un mensaje vía GET
     */
    @GetMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessageGet(@RequestParam(name = "message") String message) {
        try {
            sender.sendMessage(message);
            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "message", "Mensaje enviado: " + message
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Clase DTO para recibir el mensaje en JSON
     */
    public static class MessageRequest {
        private String message;

        public MessageRequest() {}

        public MessageRequest(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
