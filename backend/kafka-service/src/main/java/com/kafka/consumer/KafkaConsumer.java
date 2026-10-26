package com.kafka.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class KafkaConsumer {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReceivedRecord {
        private String message;
        private int partition;
        private long offset;
        private String key;
        private long timestamp;
    }

    private final List<ReceivedRecord> receivedRecords = new CopyOnWriteArrayList<>();

    /**
     * Escucha todos los mensajes del topic 'tutorial-particiones'
     *
     * Los parámetros @Header extraen metadatos de Kafka:
     * - RECEIVED_PARTITION: Número de partición (0, 1 o 2)
     * - OFFSET: Posición del mensaje en esa partición
     * - RECEIVED_KEY: La clave (puede ser null)
     */
    @KafkaListener(
            topics = "tutorial-particiones",
            groupId = "mi-grupo-educativo"
    )
    public void listen(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {

        // Imprimir información de manera clara y visual en la consola exactamente como pide la guía
        System.out.println("\n" + "=".repeat(100));
        System.out.printf(" MENSAJE RECIBIDO%n");
        System.out.printf(" 📍Partición: %d%n", partition);
        System.out.printf(" Offset: %d%n", offset);
        System.out.printf(" Key: %s%n", key != null ? key : "(sin key)");
        System.out.printf(" Mensaje: %s%n", message);
        System.out.println("=".repeat(100) + "\n");

        // Almacenar en memoria para que la interfaz Web en la nube pueda mostrarlo en vivo
        receivedRecords.add(0, new ReceivedRecord(message, partition, offset, key, System.currentTimeMillis()));
        if (receivedRecords.size() > 50) {
            receivedRecords.remove(receivedRecords.size() - 1);
        }
    }

    public List<ReceivedRecord> getReceivedRecords() {
        return receivedRecords;
    }

    public void clearRecords() {
        receivedRecords.clear();
    }
}
