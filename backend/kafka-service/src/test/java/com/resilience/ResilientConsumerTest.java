package com.resilience;

import com.resilience.consumer.ResilientConsumer;
import com.resilience.model.TransaccionSegura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Unitarios del Consumidor Resiliente
 * Valida: Idempotencia (Check-then-Act), Registro de Auditoría y Limpieza de Estado
 * Duoc UC - DSY1107 Desarrollo Cloud Native I - PDF 1
 */
class ResilientConsumerTest {

    private ResilientConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ResilientConsumer();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 1: Idempotencia (Check-then-Act)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("1.1 - Tabla de idempotencia vacía al inicio")
    void tablaIdempotenciaVaciaAlInicio() {
        Map<String, Long> processedKeys = consumer.getProcessedMessagesMap();
        assertNotNull(processedKeys, "La tabla de procesados no debe ser nula");
        assertTrue(processedKeys.isEmpty(), "La tabla de procesados debe estar vacía al inicio");
    }

    @Test
    @DisplayName("1.2 - Transacciones procesadas vacías al inicio")
    void transaccionesProcesadasVaciasAlInicio() {
        List<TransaccionSegura> txs = consumer.getProcessedTransactions();
        assertNotNull(txs, "La lista de transacciones no debe ser nula");
        assertTrue(txs.isEmpty(), "La lista de transacciones debe estar vacía al inicio");
    }

    @Test
    @DisplayName("1.3 - Audit logs vacíos al inicio")
    void auditLogsVaciosAlInicio() {
        List<Map<String, Object>> logs = consumer.getAuditLogs();
        assertNotNull(logs, "Los audit logs no deben ser nulos");
        assertTrue(logs.isEmpty(), "Los audit logs deben estar vacíos al inicio");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 2: Limpieza de Estado
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("2.1 - clearAll() limpia todos los estados internos")
    void clearAllLimpiaTodoElEstado() {
        // Ejecutar limpieza directa (clearAll es idempotente)
        consumer.clearAll();

        assertTrue(consumer.getProcessedMessagesMap().isEmpty(),
                "La tabla de idempotencia debe vaciarse con clearAll()");
        assertTrue(consumer.getProcessedTransactions().isEmpty(),
                "Las transacciones procesadas deben vaciarse con clearAll()");
        assertTrue(consumer.getAuditLogs().isEmpty(),
                "Los audit logs deben vaciarse con clearAll()");
    }

    @Test
    @DisplayName("2.2 - clearAll() puede llamarse múltiples veces sin error")
    void clearAllEsIdempotente() {
        assertDoesNotThrow(() -> {
            consumer.clearAll();
            consumer.clearAll();
            consumer.clearAll();
        }, "clearAll() debe poder llamarse múltiples veces sin lanzar excepciones");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 3: Modelo TransaccionSegura (JSR-380)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("3.1 - TransaccionSegura puede construirse con constructor completo")
    void transaccionSeguraConstructorCompleto() {
        TransaccionSegura tx = new TransaccionSegura(
                "tx-test-001",
                "juan",
                15000.0,
                "app",
                "juan@banco.cl",
                System.currentTimeMillis(),
                "none"
        );

        assertEquals("tx-test-001", tx.getMessageId());
        assertEquals("juan", tx.getUsuario());
        assertEquals(15000.0, tx.getMonto());
        assertEquals("app", tx.getOrigen());
        assertEquals("juan@banco.cl", tx.getEmail());
        assertEquals("none", tx.getSimulationErrorMode());
    }

    @Test
    @DisplayName("3.2 - TransaccionSegura puede construirse con constructor vacío (Lombok @NoArgsConstructor)")
    void transaccionSeguraConstructorVacio() {
        TransaccionSegura tx = new TransaccionSegura();

        assertNull(tx.getMessageId(), "messageId debe ser null por defecto");
        assertNull(tx.getUsuario(), "usuario debe ser null por defecto");
        assertNull(tx.getMonto(), "monto debe ser null por defecto");
    }

    @Test
    @DisplayName("3.3 - TransaccionSegura setters y getters (Lombok @Data)")
    void transaccionSeguraSettersGetters() {
        TransaccionSegura tx = new TransaccionSegura();
        tx.setMessageId("tx-setter-999");
        tx.setUsuario("maria");
        tx.setMonto(5000.0);
        tx.setOrigen("cajero");
        tx.setEmail("maria@banco.cl");
        tx.setSimulationErrorMode("transient_error");

        assertEquals("tx-setter-999", tx.getMessageId());
        assertEquals("maria", tx.getUsuario());
        assertEquals(5000.0, tx.getMonto());
        assertEquals("cajero", tx.getOrigen());
        assertEquals("maria@banco.cl", tx.getEmail());
        assertEquals("transient_error", tx.getSimulationErrorMode());
    }

    @Test
    @DisplayName("3.4 - Modos de simulación de error son los esperados")
    void modoSimulacionErrorDefinidos() {
        String[] modos = {"none", "transient_error", "permanent_error"};
        for (String modo : modos) {
            TransaccionSegura tx = new TransaccionSegura();
            tx.setSimulationErrorMode(modo);
            assertEquals(modo, tx.getSimulationErrorMode(),
                    "El modo de simulación '" + modo + "' debe poder asignarse y recuperarse correctamente");
        }
    }
}
