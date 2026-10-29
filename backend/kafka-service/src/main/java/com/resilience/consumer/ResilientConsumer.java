package com.resilience.consumer;

import com.resilience.model.TransaccionSegura;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consumidor Resiliente con Validación (JSR-380), Idempotencia (Check-then-Act)
 * y Observabilidad Contextual (MDC + Correlation ID).
 * Siguiendo los principios de Duoc UC - PDF 1.
 */
@Service
public class ResilientConsumer {

    private static final Logger log = LoggerFactory.getLogger(ResilientConsumer.class);

    // Tabla de Control de Idempotencia en memoria: processed_messages (message_id, timestamp)
    // Simula la tabla de BD con Unique Constraint (PDF 1 - Página 9)
    private final Map<String, Long> processedMessages = new ConcurrentHashMap<>();

    // Registro de reintentos por mensaje para simulación didáctica
    private final Map<String, AtomicInteger> retryCounters = new ConcurrentHashMap<>();

    // Historial para visualización en el Frontend
    private final List<Map<String, Object>> auditLogs = new CopyOnWriteArrayList<>();
    private final List<TransaccionSegura> processedTransactions = new CopyOnWriteArrayList<>();

    @KafkaListener(
            topics = "banco-resiliente",
            groupId = "banco-resiliente-group",
            containerFactory = "resilientKafkaListenerContainerFactory"
    )
    public void consumeTransaccion(
            @Valid @Payload TransaccionSegura tx,
            @Header(name = "X-Correlation-ID", required = false) String correlationIdHeader,
            @Header(name = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
            @Header(name = KafkaHeaders.OFFSET, required = false) Long offset) {

        String traceId = (correlationIdHeader != null && !correlationIdHeader.isBlank())
                ? correlationIdHeader
                : (tx.getMessageId() != null ? tx.getMessageId() : UUID.randomUUID().toString());

        // Paso Observabilidad: Inyectar Trace ID en MDC para logging contextual (PDF 1 - Página 10)
        MDC.put("traceId", traceId);

        try {
            log.info("[MDC: {}] Evento TransaccionSegura recibido ID: {}, Usuario: {}, Monto: ${}",
                    traceId, tx.getMessageId(), tx.getUsuario(), tx.getMonto());

            // --- PATRÓN DE IDEMPOTENCIA: CHECK-THEN-ACT (PDF 1 - Página 9) ---
            // Paso 1: Recibir mensaje
            // Paso 2: Intentar insertar ID en processed_messages
            String messageId = tx.getMessageId();
            Long existingTimestamp = processedMessages.putIfAbsent(messageId, System.currentTimeMillis());

            // Paso 3: Si ya existía -> Violación de Unicidad -> Duplicado detectado -> IGNORAR
            if (existingTimestamp != null) {
                String duplicateMsg = String.format("⚠️ [IDEMPOTENCIA] Duplicado detectado para ID: %s (procesado a las %s). Mensaje ignorado para evitar doble cobro.",
                        messageId, new Date(existingTimestamp));
                log.warn("[MDC: {}] {}", traceId, duplicateMsg);

                addAuditLog("IDEMPOTENT_IGNORED", traceId, messageId, duplicateMsg, partition, offset);
                return; // Ignorar sin error (Idempotente)
            }

            // Paso 4: Si se inserta bien, verificar simulaciones didácticas de error/reintento
            AtomicInteger attempts = retryCounters.computeIfAbsent(messageId, k -> new AtomicInteger(0));
            int currentAttempt = attempts.incrementAndGet();

            if ("transient_error".equalsIgnoreCase(tx.getSimulationErrorMode())) {
                if (currentAttempt <= 2) {
                    String warnMsg = String.format("Fallo al procesar ID: %s, reintentando (intento %d/3) - Error transitorio de red simulado",
                            messageId, currentAttempt);
                    log.warn("[MDC: {}] {}", traceId, warnMsg);
                    addAuditLog("RETRY_ATTEMPT", traceId, messageId, warnMsg, partition, offset);
                    throw new RuntimeException("Error transitorio simulado: Base de datos con latencia o timeout de red");
                } else {
                    log.info("[MDC: {}] Reintento exitoso tras {} intentos para ID: {}", traceId, currentAttempt, messageId);
                    addAuditLog("RETRY_SUCCESS", traceId, messageId, "Recuperación exitosa tras reintentos con Exponential Backoff", partition, offset);
                }
            } else if ("permanent_error".equalsIgnoreCase(tx.getSimulationErrorMode())) {
                String errMsg = String.format("Fallo definitivo ID: %s, agotando reintentos para envío a DLT", messageId);
                log.error("[MDC: {}] {}", traceId, errMsg);
                addAuditLog("PERMANENT_ERROR", traceId, messageId, errMsg, partition, offset);
                throw new IllegalStateException("Error permanente de negocio no recuperable");
            }

            // Paso 5: Ejecutar lógica de negocio normal
            processedTransactions.add(0, tx);
            if (processedTransactions.size() > 50) {
                processedTransactions.remove(processedTransactions.size() - 1);
            }

            String successMsg = String.format("✅ Transacción %s procesada exitosamente por $%.2f para %s",
                    messageId, tx.getMonto(), tx.getUsuario());
            log.info("[MDC: {}] {}", traceId, successMsg);
            addAuditLog("PROCESSED_SUCCESS", traceId, messageId, successMsg, partition, offset);

        } finally {
            // Limpiar contexto MDC para evitar fugas entre hilos del pool
            MDC.remove("traceId");
        }
    }

    private void addAuditLog(String status, String traceId, String messageId, String details, Integer partition, Long offset) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", UUID.randomUUID().toString());
        entry.put("timestamp", System.currentTimeMillis());
        entry.put("status", status);
        entry.put("traceId", traceId);
        entry.put("messageId", messageId);
        entry.put("details", details);
        entry.put("partition", partition != null ? partition : 0);
        entry.put("offset", offset != null ? offset : 0);

        auditLogs.add(0, entry);
        if (auditLogs.size() > 100) {
            auditLogs.remove(auditLogs.size() - 1);
        }
    }

    public List<Map<String, Object>> getAuditLogs() {
        return auditLogs;
    }

    public List<TransaccionSegura> getProcessedTransactions() {
        return processedTransactions;
    }

    public Map<String, Long> getProcessedMessagesMap() {
        return processedMessages;
    }

    public void clearAll() {
        processedMessages.clear();
        retryCounters.clear();
        auditLogs.clear();
        processedTransactions.clear();
    }
}
