package cl.duoc.telemedicina.notificaciones.controller;

import cl.duoc.telemedicina.notificaciones.dto.CreateBindingDTO;
import cl.duoc.telemedicina.notificaciones.dto.CreateExchangeDTO;
import cl.duoc.telemedicina.notificaciones.dto.CreateQueueDTO;
import cl.duoc.telemedicina.notificaciones.service.RabbitResourceManager;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * =====================================================================
 * RabbitMQAdminController: API REST de Administración Programática
 * Cumple con Pauta Duoc UC EP2:
 *  - 13% Endpoints REST bien definidos (POST /queues, DELETE /queues/{name}, etc.)
 *  - 10% Lógica completamente encapsulada en RabbitResourceManager
 *  - 7% Validación robusta con DTOs (@Valid / @NotBlank) y mensajes claros 400
 * =====================================================================
 */
@RestController
@RequestMapping("/api/rabbitmq")
@CrossOrigin(origins = "*")
public class RabbitMQAdminController {

    @Autowired
    private RabbitResourceManager resourceManager;

    // =================================================================
    // GESTIÓN DE COLAS (QUEUES)
    // =================================================================

    /**
     * Crear una cola (admite cuerpo JSON con DTO validado o parámetro queueName).
     */
    @PostMapping("/queues")
    public ResponseEntity<?> createQueue(
            @Valid @RequestBody(required = false) CreateQueueDTO dto,
            BindingResult bindingResult,
            @RequestParam(required = false) String queueName) {

        // Si se envió JSON y falló la validación
        if (dto != null && bindingResult != null && bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "code", 400,
                    "message", errorMsg
            ));
        }

        String finalName = (dto != null && dto.getQueueName() != null) ? dto.getQueueName() : queueName;
        boolean durable = dto == null || dto.isDurable();

        if (finalName == null || finalName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "code", 400,
                    "message", "El nombre de la cola es obligatorio y no puede estar vacío."
            ));
        }

        finalName = finalName.trim();
        if (!finalName.matches("^[a-zA-Z0-9._-]+$")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "code", 400,
                    "message", "El nombre de la cola '" + finalName + "' es inválido. Solo se admiten letras, números, puntos, guiones y guiones bajos."
            ));
        }

        try {
            resourceManager.createQueue(finalName, durable);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "code", 201,
                    "message", "Cola '" + finalName + "' creada exitosamente",
                    "queueName", finalName,
                    "durable", durable
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "code", 500,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Consultar información de una cola.
     */
    @GetMapping("/queues/{queueName}")
    public ResponseEntity<?> getQueueInfo(@PathVariable String queueName) {
        if (queueName == null || queueName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "El nombre de la cola es inválido."
            ));
        }
        QueueInformation info = resourceManager.getQueueInfo(queueName.trim());
        if (info == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", "La cola '" + queueName + "' no fue encontrada en RabbitMQ."
            ));
        }
        return ResponseEntity.ok(Map.of(
                "name", info.getName(),
                "messageCount", info.getMessageCount(),
                "consumerCount", info.getConsumerCount()
        ));
    }

    /**
     * Eliminar una cola.
     */
    @DeleteMapping("/queues/{queueName}")
    public ResponseEntity<?> deleteQueue(@PathVariable String queueName) {
        if (queueName == null || queueName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "El nombre de la cola no puede estar vacío."
            ));
        }
        try {
            resourceManager.deleteQueue(queueName.trim());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Cola '" + queueName.trim() + "' eliminada exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Purgar una cola.
     */
    @PostMapping("/queues/{queueName}/purge")
    public ResponseEntity<?> purgeQueue(@PathVariable String queueName) {
        if (queueName == null || queueName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "El nombre de la cola no puede estar vacío."
            ));
        }
        try {
            int purged = resourceManager.purgeQueue(queueName.trim());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Cola '" + queueName.trim() + "' purgada exitosamente",
                    "purgedMessages", purged
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // =================================================================
    // GESTIÓN DE EXCHANGES
    // =================================================================

    /**
     * Crear un exchange (admite cuerpo JSON con DTO validado o parámetro exchangeName).
     */
    @PostMapping("/exchanges")
    public ResponseEntity<?> createExchange(
            @Valid @RequestBody(required = false) CreateExchangeDTO dto,
            BindingResult bindingResult,
            @RequestParam(required = false) String exchangeName,
            @RequestParam(required = false, defaultValue = "direct") String type) {

        if (dto != null && bindingResult != null && bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", errorMsg
            ));
        }

        String finalName = (dto != null && dto.getExchangeName() != null) ? dto.getExchangeName() : exchangeName;
        String finalType = (dto != null && dto.getType() != null) ? dto.getType() : type;
        boolean durable = dto == null || dto.isDurable();

        if (finalName == null || finalName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "El nombre del exchange no puede estar vacío."
            ));
        }

        finalName = finalName.trim();
        if (!finalName.matches("^[a-zA-Z0-9._-]+$")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "El nombre del exchange '" + finalName + "' es inválido."
            ));
        }

        try {
            resourceManager.createExchange(finalName, finalType, durable);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "code", 201,
                    "message", "Exchange '" + finalName + "' creado exitosamente",
                    "exchangeName", finalName,
                    "type", finalType
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Eliminar un exchange.
     */
    @DeleteMapping("/exchanges/{exchangeName}")
    public ResponseEntity<?> deleteExchange(@PathVariable String exchangeName) {
        if (exchangeName == null || exchangeName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "El nombre del exchange no puede estar vacío."
            ));
        }
        try {
            resourceManager.deleteExchange(exchangeName.trim());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Exchange '" + exchangeName.trim() + "' eliminado exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // =================================================================
    // GESTIÓN DE BINDINGS
    // =================================================================

    /**
     * Crear un binding.
     */
    @PostMapping("/bindings")
    public ResponseEntity<?> createBinding(
            @Valid @RequestBody(required = false) CreateBindingDTO dto,
            BindingResult bindingResult,
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) String exchangeName,
            @RequestParam(required = false) String routingKey) {

        if (dto != null && bindingResult != null && bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", errorMsg
            ));
        }

        String q = (dto != null && dto.getQueueName() != null) ? dto.getQueueName() : queueName;
        String e = (dto != null && dto.getExchangeName() != null) ? dto.getExchangeName() : exchangeName;
        String rk = (dto != null && dto.getRoutingKey() != null) ? dto.getRoutingKey() : routingKey;

        if (q == null || q.trim().isEmpty() || e == null || e.trim().isEmpty() || rk == null || rk.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Los parámetros de binding (queueName, exchangeName, routingKey) son obligatorios y no pueden estar vacíos."
            ));
        }

        try {
            resourceManager.createBinding(q.trim(), e.trim(), rk.trim());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "code", 201,
                    "message", "Binding creado: " + q.trim() + " <-> " + e.trim() + " [routingKey: " + rk.trim() + "]"
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", ex.getMessage()
            ));
        }
    }

    /**
     * Eliminar un binding.
     */
    @DeleteMapping("/bindings")
    public ResponseEntity<?> deleteBinding(
            @RequestParam String queueName,
            @RequestParam String exchangeName,
            @RequestParam String routingKey) {

        if (queueName == null || queueName.trim().isEmpty() ||
                exchangeName == null || exchangeName.trim().isEmpty() ||
                routingKey == null || routingKey.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Los parámetros de binding (queueName, exchangeName, routingKey) son obligatorios."
            ));
        }

        try {
            resourceManager.deleteBinding(queueName.trim(), exchangeName.trim(), routingKey.trim());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Binding eliminado: " + queueName.trim() + " <-> " + exchangeName.trim()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", ex.getMessage()
            ));
        }
    }
}
