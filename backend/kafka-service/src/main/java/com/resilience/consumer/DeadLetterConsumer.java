package com.resilience.consumer;

import com.resilience.model.DltRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Consumidor del Dead Letter Topic (.DLT) / "Sala de Emergencias" de Mensajes
 * Siguiendo la Guía de Manejo de Errores en Streaming (Duoc UC - PDF 1, Página 8).
 *
 * Captura todos los mensajes que agotaron reintentos o fallaron permanentemente,
 * extrayendo los headers enriquecidos (Header Enrichment) generados por Spring:
 * - Mensaje de excepción
 * - Stacktrace
 * - Tópico original
 * - Correlation ID
 */
@Service
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    private final List<DltRecord> dltRecords = new CopyOnWriteArrayList<>();

    @KafkaListener(
            topics = "banco-resiliente.DLT",
            groupId = "banco-dlt-inspector-group"
    )
    public void listenDlt(ConsumerRecord<String, Object> record) {
        Map<String, String> headerMap = new HashMap<>();
        String exMessage = "Error no especificado";
        String exStacktrace = "";
        String origTopic = record.topic();
        String correlationId = null;

        for (Header h : record.headers()) {
            String val = new String(h.value(), StandardCharsets.UTF_8);
            headerMap.put(h.key(), val);

            if (h.key().contains("exception-message")) {
                exMessage = val;
            } else if (h.key().contains("exception-stacktrace")) {
                exStacktrace = val.length() > 500 ? val.substring(0, 500) + "..." : val;
            } else if (h.key().contains("original-topic")) {
                origTopic = val;
            } else if (h.key().equalsIgnoreCase("X-Correlation-ID")) {
                correlationId = val;
            }
        }

        String payloadStr = record.value() != null ? record.value().toString() : "(null)";

        DltRecord dltRecord = new DltRecord(
                UUID.randomUUID().toString(),
                origTopic,
                record.partition(),
                record.offset(),
                record.key(),
                payloadStr,
                exMessage,
                exStacktrace,
                correlationId,
                System.currentTimeMillis(),
                headerMap
        );

        dltRecords.add(0, dltRecord);
        if (dltRecords.size() > 50) {
            dltRecords.remove(dltRecords.size() - 1);
        }

        log.error("💀 [DLT RECIBIDO] Mensaje desviado a DLT. Causa: {} | Correlation-ID: {} | Payload: {}",
                exMessage, correlationId, payloadStr);
    }

    public List<DltRecord> getDltRecords() {
        return dltRecords;
    }

    public Optional<DltRecord> findById(String id) {
        return dltRecords.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public void removeById(String id) {
        dltRecords.removeIf(r -> r.getId().equals(id));
    }

    public void clear() {
        dltRecords.clear();
    }
}
