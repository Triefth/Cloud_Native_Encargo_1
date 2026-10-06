package cl.duoc.telemedicina.notificaciones.controller;

import cl.duoc.telemedicina.notificaciones.service.RabbitResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rabbitmq")
@CrossOrigin(origins = "*")
public class RabbitMQAdminController {

    @Autowired
    private RabbitResourceManager resourceManager;

    @PostMapping("/queues")
    public ResponseEntity<String> createQueue(@RequestParam String queueName) {
        if (queueName == null || queueName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El nombre de la cola no puede estar vacío.");
        }
        resourceManager.createQueue(queueName.trim());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Cola '" + queueName.trim() + "' creada exitosamente");
    }

    @PostMapping("/exchanges")
    public ResponseEntity<String> createExchange(@RequestParam String exchangeName) {
        if (exchangeName == null || exchangeName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El nombre del exchange no puede estar vacío.");
        }
        resourceManager.createExchange(exchangeName.trim());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Exchange '" + exchangeName.trim() + "' creado exitosamente");
    }

    @PostMapping("/bindings")
    public ResponseEntity<String> createBinding(
            @RequestParam String queueName,
            @RequestParam String exchangeName,
            @RequestParam String routingKey) {
        if (queueName == null || queueName.trim().isEmpty() ||
            exchangeName == null || exchangeName.trim().isEmpty() ||
            routingKey == null || routingKey.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Los parámetros de binding (queueName, exchangeName, routingKey) no pueden estar vacíos.");
        }
        resourceManager.createBinding(queueName.trim(), exchangeName.trim(), routingKey.trim());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Binding creado: " + queueName.trim() + " <-> " + exchangeName.trim());
    }

    @GetMapping("/queues/{queueName}")
    public ResponseEntity<?> getQueueInfo(@PathVariable String queueName) {
        if (queueName == null || queueName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El nombre de la cola es inválido.");
        }
        var info = resourceManager.getQueueInfo(queueName.trim());
        return info != null ? ResponseEntity.ok(info) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/queues/{queueName}")
    public ResponseEntity<String> deleteQueue(@PathVariable String queueName) {
        if (queueName == null || queueName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El nombre de la cola no puede estar vacío.");
        }
        resourceManager.deleteQueue(queueName.trim());
        return ResponseEntity.ok("Cola '" + queueName.trim() + "' eliminada");
    }

    @DeleteMapping("/exchanges/{exchangeName}")
    public ResponseEntity<String> deleteExchange(@PathVariable String exchangeName) {
        if (exchangeName == null || exchangeName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El nombre del exchange no puede estar vacío.");
        }
        resourceManager.deleteExchange(exchangeName.trim());
        return ResponseEntity.ok("Exchange '" + exchangeName.trim() + "' eliminado");
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<String> deleteBinding(
            @RequestParam String queueName,
            @RequestParam String exchangeName,
            @RequestParam String routingKey) {
        if (queueName == null || queueName.trim().isEmpty() ||
            exchangeName == null || exchangeName.trim().isEmpty() ||
            routingKey == null || routingKey.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Los parámetros de binding (queueName, exchangeName, routingKey) no pueden estar vacíos.");
        }
        resourceManager.deleteBinding(queueName.trim(), exchangeName.trim(), routingKey.trim());
        return ResponseEntity.ok("Binding eliminado: " + queueName.trim() + " <-> " + exchangeName.trim());
    }

    @PostMapping("/queues/{queueName}/purge")
    public ResponseEntity<String> purgeQueue(@PathVariable String queueName) {
        if (queueName == null || queueName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El nombre de la cola no puede estar vacío.");
        }
        resourceManager.purgeQueue(queueName.trim());
        return ResponseEntity.ok("Cola '" + queueName.trim() + "' purgada");
    }
}
