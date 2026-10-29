package com.resilience.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilience.consumer.DeadLetterConsumer;
import com.resilience.consumer.ResilientConsumer;
import com.resilience.model.DltRecord;
import com.resilience.model.TransaccionSegura;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Controlador REST para Demostración del Manejo de Errores, DLT, Idempotencia y Observabilidad
 * Siguiendo los principios de Duoc UC - PDF 1.
 */
@RestController
@RequestMapping({"/api/resilience", "/api/kafka/resilience"})
@CrossOrigin(origins = "*")
public class ResilienceController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final ResilientConsumer resilientConsumer;
    private final DeadLetterConsumer deadLetterConsumer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ResilienceController(
            @Qualifier("resilientKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate,
            @Qualifier("stringKafkaTemplate") KafkaTemplate<String, String> stringKafkaTemplate,
            ResilientConsumer resilientConsumer,
            DeadLetterConsumer deadLetterConsumer) {
        this.kafkaTemplate = kafkaTemplate;
        this.stringKafkaTemplate = stringKafkaTemplate;
        this.resilientConsumer = resilientConsumer;
        this.deadLetterConsumer = deadLetterConsumer;
    }

    /**
     * Enviar transacción normal con trazabilidad (Correlation ID)
     */
    @PostMapping("/send")
    public Map<String, Object> sendTransaction(@RequestBody TransaccionSegura tx) {
        if (tx.getMessageId() == null || tx.getMessageId().isBlank()) {
            tx.setMessageId("tx-" + UUID.randomUUID().toString().substring(0, 8));
        }
        if (tx.getTimestamp() == null) {
            tx.setTimestamp(System.currentTimeMillis());
        }

        String correlationId = "corr-" + UUID.randomUUID().toString().substring(0, 8);

        ProducerRecord<String, Object> record = new ProducerRecord<>("banco-resiliente", tx.getUsuario(), tx);
        record.headers().add(new RecordHeader("X-Correlation-ID", correlationId.getBytes(StandardCharsets.UTF_8)));

        kafkaTemplate.send(record);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("messageId", tx.getMessageId());
        response.put("correlationId", correlationId);
        response.put("topic", "banco-resiliente");
        response.put("message", "Transacción enviada con Header de Trazabilidad X-Correlation-ID: " + correlationId);
        return response;
    }

    /**
     * Simulación 1: Envío con Fallo de Validación JSR-380 (Bean Validation)
     * Monto negativo o campos obligatorios faltantes
     */
    @PostMapping("/simulate-validation-error")
    public Map<String, Object> simulateValidationError(@RequestParam(defaultValue = "monto_negativo") String type) {
        TransaccionSegura badTx = new TransaccionSegura();
        badTx.setMessageId("bad-val-" + UUID.randomUUID().toString().substring(0, 6));
        badTx.setTimestamp(System.currentTimeMillis());

        if ("monto_negativo".equals(type)) {
            badTx.setUsuario("carlos");
            badTx.setMonto(-500.0); // Falla @Min(1)
            badTx.setOrigen("app");
            badTx.setEmail("carlos@banco.cl");
        } else if ("email_invalido".equals(type)) {
            badTx.setUsuario("maria");
            badTx.setMonto(15000.0);
            badTx.setOrigen("web");
            badTx.setEmail("correo-sin-arroba"); // Falla @Email
        } else {
            badTx.setUsuario(""); // Falla @NotBlank
            badTx.setMonto(2000.0);
            badTx.setOrigen("cajero");
            badTx.setEmail("test@banco.cl");
        }

        String correlationId = "corr-val-error-" + UUID.randomUUID().toString().substring(0, 6);
        ProducerRecord<String, Object> record = new ProducerRecord<>("banco-resiliente", badTx.getUsuario(), badTx);
        record.headers().add(new RecordHeader("X-Correlation-ID", correlationId.getBytes(StandardCharsets.UTF_8)));

        kafkaTemplate.send(record);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("simulatedType", type);
        res.put("messageId", badTx.getMessageId());
        res.put("correlationId", correlationId);
        res.put("description", "Mensaje con carga inválida enviado. El validador JSR-380 detectará el error y enviará directamente al DLT.");
        return res;
    }

    /**
     * Simulación 2: Poison Pill (Error de Deserialización)
     * Envía bytes o JSON mal formado al topic que espera JSON
     */
    @PostMapping("/simulate-poison-pill")
    public Map<String, Object> simulatePoisonPill() {
        String poisonPill = "POISON_PILL_NO_ES_JSON_{{[[corrupt-data-binary-stream]]}}";
        String correlationId = "corr-poison-" + UUID.randomUUID().toString().substring(0, 6);

        ProducerRecord<String, String> record = new ProducerRecord<>("banco-resiliente", "poison-key", poisonPill);
        record.headers().add(new RecordHeader("X-Correlation-ID", correlationId.getBytes(StandardCharsets.UTF_8)));

        stringKafkaTemplate.send(record);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("correlationId", correlationId);
        res.put("payload", poisonPill);
        res.put("description", "Poison Pill enviada. ErrorHandlingDeserializer intercepta la falla sin provocar bucle infinito de reinicio.");
        return res;
    }

    /**
     * Simulación 3: Prueba de Idempotencia del Consumidor (Check-then-Act)
     * Envía dos mensajes con EXACTAMENTE el mismo messageId
     */
    @PostMapping("/simulate-duplicate")
    public Map<String, Object> simulateDuplicate(@RequestParam(required = false) String customId) {
        String sameId = customId != null && !customId.isBlank() ? customId : "idem-tx-" + UUID.randomUUID().toString().substring(0, 6);

        TransaccionSegura tx = new TransaccionSegura(
                sameId,
                "andres",
                45000.0,
                "web",
                "andres@banco.cl",
                System.currentTimeMillis(),
                "none"
        );

        // Envío 1
        String corr1 = "corr-dup-1-" + UUID.randomUUID().toString().substring(0, 4);
        ProducerRecord<String, Object> rec1 = new ProducerRecord<>("banco-resiliente", tx.getUsuario(), tx);
        rec1.headers().add(new RecordHeader("X-Correlation-ID", corr1.getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(rec1);

        // Envío 2 (Duplicado con mismo ID)
        String corr2 = "corr-dup-2-" + UUID.randomUUID().toString().substring(0, 4);
        ProducerRecord<String, Object> rec2 = new ProducerRecord<>("banco-resiliente", tx.getUsuario(), tx);
        rec2.headers().add(new RecordHeader("X-Correlation-ID", corr2.getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(rec2);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("duplicatedMessageId", sameId);
        res.put("description", "Se enviaron 2 eventos con el ID: " + sameId + ". El primer mensaje se procesa; el segundo es detectado por la tabla processed_messages y se ignora.");
        return res;
    }

    /**
     * Simulación 4: Reintentos con Exponential Backoff ante Error Transitorio
     */
    @PostMapping("/simulate-transient-error")
    public Map<String, Object> simulateTransientError() {
        String messageId = "retry-tx-" + UUID.randomUUID().toString().substring(0, 6);
        TransaccionSegura tx = new TransaccionSegura(
                messageId,
                "felipe",
                30000.0,
                "app",
                "felipe@banco.cl",
                System.currentTimeMillis(),
                "transient_error"
        );

        String corr = "corr-transient-" + UUID.randomUUID().toString().substring(0, 4);
        ProducerRecord<String, Object> rec = new ProducerRecord<>("banco-resiliente", tx.getUsuario(), tx);
        rec.headers().add(new RecordHeader("X-Correlation-ID", corr.getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(rec);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("messageId", messageId);
        res.put("description", "Evento con error transitorio enviado. DefaultErrorHandler aplicará Exponential Backoff antes de recuperar el mensaje.");
        return res;
    }

    /**
     * Simulación 5: Error Permanente -> Enrutamiento automático a DLT
     */
    @PostMapping("/simulate-permanent-error")
    public Map<String, Object> simulatePermanentError() {
        String messageId = "dlt-tx-" + UUID.randomUUID().toString().substring(0, 6);
        TransaccionSegura tx = new TransaccionSegura(
                messageId,
                "valentina",
                89000.0,
                "cajero",
                "valentina@banco.cl",
                System.currentTimeMillis(),
                "permanent_error"
        );

        String corr = "corr-permanent-" + UUID.randomUUID().toString().substring(0, 4);
        ProducerRecord<String, Object> rec = new ProducerRecord<>("banco-resiliente", tx.getUsuario(), tx);
        rec.headers().add(new RecordHeader("X-Correlation-ID", corr.getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(rec);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("messageId", messageId);
        res.put("description", "Error permanente enviado. Tras agotar reintentos, caerá a 'banco-resiliente.DLT' con headers enriquecidos.");
        return res;
    }

    /**
     * Listar mensajes almacenados en Dead Letter Topic (DLT)
     */
    @GetMapping("/dlt")
    public List<DltRecord> getDltRecords() {
        return deadLetterConsumer.getDltRecords();
    }

    /**
     * Herramienta de Reprocesamiento: Re-inyectar un mensaje desde la DLT al topic principal
     * (PDF 1 - Página 8)
     */
    @PostMapping("/dlt/reprocess/{id}")
    public Map<String, Object> reprocessDltMessage(@PathVariable String id) {
        Optional<DltRecord> optionalRecord = deadLetterConsumer.findById(id);

        if (optionalRecord.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", true);
            err.put("message", "No se encontró el mensaje DLT con ID: " + id);
            return err;
        }

        DltRecord dltRecord = optionalRecord.get();

        // Limpiar de DLT una vez reinyectado
        deadLetterConsumer.removeById(id);

        // Crear una nueva transacción corregida (sin simulation error)
        TransaccionSegura reprocessedTx = new TransaccionSegura(
                "reprocessed-" + UUID.randomUUID().toString().substring(0, 6),
                dltRecord.getKey() != null ? dltRecord.getKey() : "usuario-reprocesado",
                25000.0,
                "reproceso-admin",
                "auditoria@banco.cl",
                System.currentTimeMillis(),
                "none" // sin errores para que pase con éxito
        );

        String newCorrId = "corr-reprocess-" + UUID.randomUUID().toString().substring(0, 6);
        ProducerRecord<String, Object> record = new ProducerRecord<>("banco-resiliente", reprocessedTx.getUsuario(), reprocessedTx);
        record.headers().add(new RecordHeader("X-Correlation-ID", newCorrId.getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(record);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("reprocessedId", id);
        res.put("newMessageId", reprocessedTx.getMessageId());
        res.put("correlationId", newCorrId);
        res.put("message", "Mensaje re-inyectado al topic principal banco-resiliente exitosamente.");
        return res;
    }

    /**
     * Limpiar DLT
     */
    @DeleteMapping("/dlt")
    public Map<String, Object> clearDlt() {
        deadLetterConsumer.clear();
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Cola DLT vaciada.");
        return res;
    }

    /**
     * Consultar logs de auditoría de resiliencia
     */
    @GetMapping("/audit-logs")
    public List<Map<String, Object>> getAuditLogs() {
        return resilientConsumer.getAuditLogs();
    }

    /**
     * Consultar transacciones procesadas con éxito
     */
    @GetMapping("/processed")
    public List<TransaccionSegura> getProcessedTransactions() {
        return resilientConsumer.getProcessedTransactions();
    }

    /**
     * Consultar la tabla de idempotencia processed_messages
     */
    @GetMapping("/processed-keys")
    public Map<String, Long> getProcessedKeys() {
        return resilientConsumer.getProcessedMessagesMap();
    }

    /**
     * Reiniciar todo el estado de pruebas de resiliencia
     */
    @PostMapping("/reset")
    public Map<String, Object> resetAll() {
        resilientConsumer.clearAll();
        deadLetterConsumer.clear();
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Estado de resiliencia, DLT y tabla de idempotencia reiniciados.");
        return res;
    }
}
