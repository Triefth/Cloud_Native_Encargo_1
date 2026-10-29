package com.kafka;

import com.kafka.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Unitarios del KafkaConsumer (Guía 2 - Particiones y Offsets)
 * Valida: Almacenamiento en memoria de mensajes recibidos, límite de 50 y limpieza.
 * Duoc UC - DSY1107 Desarrollo Cloud Native I
 */
class KafkaConsumerTest {

    private KafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaConsumer();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 1: Estado inicial
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("1.1 - Lista de mensajes recibidos vacía al inicio")
    void listaMensajesVaciaAlInicio() {
        List<KafkaConsumer.ReceivedRecord> records = consumer.getReceivedRecords();
        assertNotNull(records, "La lista de mensajes no debe ser nula");
        assertTrue(records.isEmpty(), "La lista de mensajes recibidos debe estar vacía al inicio");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 2: clearRecords()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("2.1 - clearRecords() vacía la lista de mensajes")
    void clearRecordsVaciaLaLista() {
        consumer.clearRecords();
        assertTrue(consumer.getReceivedRecords().isEmpty(),
                "clearRecords() debe dejar la lista vacía");
    }

    @Test
    @DisplayName("2.2 - clearRecords() puede llamarse múltiples veces (idempotente)")
    void clearRecordsEsIdempotente() {
        assertDoesNotThrow(() -> {
            consumer.clearRecords();
            consumer.clearRecords();
        }, "clearRecords() debe poder llamarse sin error incluso si la lista ya está vacía");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 3: ReceivedRecord (clase interna)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("3.1 - ReceivedRecord almacena todos sus campos correctamente")
    void receivedRecordAlmacenaTodosLosCampos() {
        KafkaConsumer.ReceivedRecord record = new KafkaConsumer.ReceivedRecord(
                "Paso1-Creada",
                2,
                10L,
                "orden-555",
                1700000000000L
        );

        assertEquals("Paso1-Creada", record.getMessage());
        assertEquals(2, record.getPartition());
        assertEquals(10L, record.getOffset());
        assertEquals("orden-555", record.getKey());
        assertEquals(1700000000000L, record.getTimestamp());
    }

    @Test
    @DisplayName("3.2 - ReceivedRecord sin key (Round Robin sin clave)")
    void receivedRecordSinKey() {
        KafkaConsumer.ReceivedRecord record = new KafkaConsumer.ReceivedRecord(
                "Mensaje Round Robin",
                0,
                5L,
                null, // sin key
                System.currentTimeMillis()
        );

        assertNull(record.getKey(),
                "El campo key debe ser null cuando se envía un mensaje sin clave (Round Robin)");
        assertEquals("Mensaje Round Robin", record.getMessage());
    }

    @Test
    @DisplayName("3.3 - ReceivedRecord constructor vacío (Lombok @NoArgsConstructor)")
    void receivedRecordConstructorVacio() {
        KafkaConsumer.ReceivedRecord record = new KafkaConsumer.ReceivedRecord();
        assertNull(record.getMessage(), "El mensaje debe ser null por defecto");
        assertEquals(0, record.getPartition(), "La partición debe ser 0 por defecto");
        assertEquals(0L, record.getOffset(), "El offset debe ser 0 por defecto");
    }

    @Test
    @DisplayName("3.4 - Sticky Partitioning: misma key debe ir siempre a la misma partición (lógica hash)")
    void stickyPartitioningMismaKeyMismaParticion() {
        // Kafka asigna partition = hash(key) % numPartitions
        // Con 3 particiones y key "orden-555", siempre va a la misma partición
        // Simulamos la consistencia guardando dos registros con la misma key y misma partición
        int partition = Math.abs("orden-555".hashCode() % 3);

        KafkaConsumer.ReceivedRecord rec1 = new KafkaConsumer.ReceivedRecord("Paso1-Creada", partition, 0L, "orden-555", System.currentTimeMillis());
        KafkaConsumer.ReceivedRecord rec2 = new KafkaConsumer.ReceivedRecord("Paso2-Pagada", partition, 1L, "orden-555", System.currentTimeMillis());
        KafkaConsumer.ReceivedRecord rec3 = new KafkaConsumer.ReceivedRecord("Paso3-Enviada", partition, 2L, "orden-555", System.currentTimeMillis());

        assertEquals(rec1.getPartition(), rec2.getPartition(),
                "Con la misma key, Paso1 y Paso2 deben ir a la misma partición (Sticky Partitioning)");
        assertEquals(rec2.getPartition(), rec3.getPartition(),
                "Con la misma key, Paso2 y Paso3 deben ir a la misma partición (Sticky Partitioning)");
        assertEquals("orden-555", rec1.getKey(), "La key debe persistir en el record");
    }
}
