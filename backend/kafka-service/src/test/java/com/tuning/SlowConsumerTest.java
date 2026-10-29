package com.tuning;

import com.tuning.consumer.SlowConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Unitarios del SlowConsumer - Simulador de Consumer Lag
 * Valida: Estado inicial, control de pausa, retardo configurable, reset
 * Duoc UC - DSY1107 Desarrollo Cloud Native I - PDF 3 (Consumer Lag)
 */
class SlowConsumerTest {

    private SlowConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new SlowConsumer();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 1: Estado inicial
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("1.1 - Contador de mensajes procesados inicia en cero")
    void contadorProcesadosIniciaEnCero() {
        assertEquals(0L, consumer.getProcessedCount(),
                "El contador de mensajes procesados debe comenzar en 0");
    }

    @Test
    @DisplayName("1.2 - Retardo inicial es 10ms (valor de guía PDF 3)")
    void retardoInicialEs10ms() {
        assertEquals(10, consumer.getDelayMs(),
                "El retardo inicial del consumidor lento debe ser 10ms según la Guía PDF 3");
    }

    @Test
    @DisplayName("1.3 - Consumidor no está pausado al iniciar")
    void consumidorNoEstaPausadoAlInicio() {
        assertFalse(consumer.isPaused(),
                "El consumidor no debe estar pausado al iniciar");
    }

    @Test
    @DisplayName("1.4 - Lista de muestras vacía al inicio")
    void listaMuestrasVaciaAlInicio() {
        List<SlowConsumer.ConsumerMetricRecord> samples = consumer.getSampleRecords();
        assertNotNull(samples, "La lista de muestras no debe ser nula");
        assertTrue(samples.isEmpty(), "La lista de muestras debe estar vacía al inicio");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 2: Control de pausa
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("2.1 - setPaused(true) pausa el consumidor")
    void setPausedTruePausaElConsumidor() {
        consumer.setPaused(true);
        assertTrue(consumer.isPaused(), "isPaused() debe retornar true tras setPaused(true)");
    }

    @Test
    @DisplayName("2.2 - setPaused(false) reanuda el consumidor pausado")
    void setPausedFalseReanudaElConsumidor() {
        consumer.setPaused(true);
        consumer.setPaused(false);
        assertFalse(consumer.isPaused(), "isPaused() debe retornar false tras setPaused(false)");
    }

    @Test
    @DisplayName("2.3 - setPaused() puede alternarse múltiples veces")
    void setPausedPuedeAlternarse() {
        consumer.setPaused(true);
        consumer.setPaused(false);
        consumer.setPaused(true);
        assertTrue(consumer.isPaused(), "La última asignación de pausa debe ser la que prevalece");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 3: Control de retardo (delay)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("3.1 - setDelayMs() actualiza el retardo correctamente")
    void setDelayMsActualizaRetardo() {
        consumer.setDelayMs(30);
        assertEquals(30, consumer.getDelayMs(), "El retardo debe actualizarse a 30ms");
    }

    @Test
    @DisplayName("3.2 - setDelayMs(0) permite consumo instantáneo (drenar lag)")
    void setDelayMsCeroPermiteConsumoCero() {
        consumer.setDelayMs(0);
        assertEquals(0, consumer.getDelayMs(),
                "setDelayMs(0) debe resultar en 0ms de retardo para drenar el Consumer Lag");
    }

    @Test
    @DisplayName("3.3 - setDelayMs() con valor negativo usa 0 (protección mínima)")
    void setDelayMsNegativoUsaCero() {
        consumer.setDelayMs(-10);
        assertEquals(0, consumer.getDelayMs(),
                "Un retardo negativo debe ser tratado como 0 (Math.max protección)");
    }

    @Test
    @DisplayName("3.4 - setDelayMs(50) genera lag extremo (máx 20 msg/seg)")
    void setDelayMs50GeneraLagExtremo() {
        consumer.setDelayMs(50);
        assertEquals(50, consumer.getDelayMs(),
                "Con 50ms de retardo, el consumidor sólo puede procesar ~20 msg/seg (lag alto)");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 4: Reset y limpieza
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("4.1 - reset() restablece contadores a cero")
    void resetRestablecedores() {
        // Modificar el estado artificialmente con setters
        consumer.setDelayMs(50);
        consumer.setPaused(true);

        // Ahora reseteamos
        consumer.reset();

        assertEquals(0L, consumer.getProcessedCount(),
                "El contador de procesados debe reiniciarse a 0 tras reset()");
        assertTrue(consumer.getSampleRecords().isEmpty(),
                "Las muestras deben vaciarse tras reset()");
    }

    @Test
    @DisplayName("4.2 - reset() puede llamarse múltiples veces (idempotente)")
    void resetEsIdempotente() {
        assertDoesNotThrow(() -> {
            consumer.reset();
            consumer.reset();
        }, "reset() debe poder llamarse múltiples veces sin lanzar excepciones");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bloque 5: ConsumerMetricRecord (clase interna)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("5.1 - ConsumerMetricRecord almacena key, partición, offset y timestamp")
    void consumerMetricRecordAlmacenaDatos() {
        SlowConsumer.ConsumerMetricRecord record =
                new SlowConsumer.ConsumerMetricRecord("sensor-2", 1, 100L, 1700000000000L);

        assertEquals("sensor-2", record.getKey());
        assertEquals(1, record.getPartition());
        assertEquals(100L, record.getOffset());
        assertEquals(1700000000000L, record.getTimestamp());
    }
}

