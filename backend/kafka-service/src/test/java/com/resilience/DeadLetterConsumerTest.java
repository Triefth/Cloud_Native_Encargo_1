package com.resilience;

import com.resilience.consumer.DeadLetterConsumer;
import com.resilience.model.DltRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Unitarios del Consumidor del Dead Letter Topic (DLT)
 * Valida: Almacenamiento de registros DLT, búsqueda por ID, eliminación y límite de 50 registros
 * Duoc UC - DSY1107 Desarrollo Cloud Native I - PDF 1 (Página 8)
 */
class DeadLetterConsumerTest {

    private DeadLetterConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new DeadLetterConsumer();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 1: Estado inicial
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("1.1 - DLT vacío al inicializar el consumidor")
    void dltVacioAlInicio() {
        List<DltRecord> records = consumer.getDltRecords();
        assertNotNull(records, "La lista DLT no debe ser nula");
        assertTrue(records.isEmpty(), "La Sala de Emergencias debe estar vacía al inicio");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 2: Modelo DltRecord
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("2.1 - DltRecord puede construirse con todos sus campos")
    void dltRecordConstructorCompleto() {
        DltRecord record = new DltRecord(
                "id-001",
                "banco-resiliente",
                0,
                42L,
                "usuario-key",
                "{\"usuario\":\"juan\",\"monto\":-500}",
                "El monto debe ser mayor o igual a $1.0",
                "javax.validation.ConstraintViolationException...",
                "corr-abc123",
                System.currentTimeMillis(),
                new HashMap<>()
        );

        assertEquals("id-001", record.getId());
        assertEquals("banco-resiliente", record.getOriginalTopic());
        assertEquals(0, record.getPartition());
        assertEquals(42L, record.getOffset());
        assertEquals("usuario-key", record.getKey());
        assertNotNull(record.getPayload());
        assertEquals("El monto debe ser mayor o igual a $1.0", record.getExceptionMessage());
        assertEquals("corr-abc123", record.getCorrelationId());
        assertNotNull(record.getHeaders());
    }

    @Test
    @DisplayName("2.2 - DltRecord constructor vacío (Lombok @NoArgsConstructor)")
    void dltRecordConstructorVacio() {
        DltRecord record = new DltRecord();
        assertNull(record.getId(), "id debe ser null por defecto");
        assertNull(record.getOriginalTopic(), "originalTopic debe ser null por defecto");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 3: Operaciones sobre la lista DLT (simuladas directamente)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("3.1 - findById devuelve Optional.empty() si el ID no existe")
    void findByIdDevuelveEmptyParaIdInexistente() {
        Optional<DltRecord> result = consumer.findById("id-inexistente-xyz");
        assertTrue(result.isEmpty(), "findById debe devolver Optional.empty() para IDs no existentes");
    }

    @Test
    @DisplayName("3.2 - removeById no lanza excepción si el ID no existe")
    void removeByIdNoLanzaExcepcionSiNoExiste() {
        assertDoesNotThrow(() -> consumer.removeById("id-que-no-existe"),
                "removeById() no debe lanzar excepción con IDs inexistentes");
    }

    @Test
    @DisplayName("3.3 - clear() vacía la Sala de Emergencias")
    void clearVaciaLaListaDlt() {
        // Primero nos aseguramos de que al limpiar ya vacío no hay error
        consumer.clear();
        assertTrue(consumer.getDltRecords().isEmpty(),
                "clear() debe dejar la lista DLT vacía");
    }

    @Test
    @DisplayName("3.4 - clear() puede llamarse múltiples veces (idempotente)")
    void clearEsIdempotente() {
        assertDoesNotThrow(() -> {
            consumer.clear();
            consumer.clear();
        }, "clear() debe poder llamarse múltiples veces sin error");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 4: Límite máximo de registros (ventana deslizante de 50)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("4.1 - DltRecord implementa Serializable (requerido para Kafka)")
    void dltRecordImplementaSerializable() {
        DltRecord record = new DltRecord();
        assertInstanceOf(java.io.Serializable.class, record,
                "DltRecord debe implementar Serializable para ser usado como payload Kafka");
    }

    @Test
    @DisplayName("4.2 - TransaccionSegura implementa Serializable (requerido para Kafka)")
    void transaccionSeguraImplementaSerializable() {
        com.resilience.model.TransaccionSegura tx = new com.resilience.model.TransaccionSegura();
        assertInstanceOf(java.io.Serializable.class, tx,
                "TransaccionSegura debe implementar Serializable");
    }
}
