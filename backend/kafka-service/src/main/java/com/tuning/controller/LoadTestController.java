package com.tuning.controller;

import com.tuning.consumer.SlowConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/**
 * Generador de Carga Masiva (Load Tester) & Monitoreo de Consumer Lag
 * Siguiendo la Guía de Tuning y Pruebas de Carga (Duoc UC 2025 - PDF 3) y Escalamiento (PDF 2).
 *
 * Utiliza un productor afinado (batching de 32KB, linger 20ms, compresión snappy)
 * para disparar miles de mensajes por segundo en paralelo, estresando el sistema
 * y generando Consumer Lag visible frente al SlowConsumer.
 */
@RestController
@RequestMapping({"/api/test", "/api/tuning"})
@CrossOrigin(origins = "*")
public class LoadTestController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SlowConsumer slowConsumer;

    private final AtomicLong totalProducedCount = new AtomicLong(0);
    private volatile double lastThroughput = 0.0;
    private volatile long lastDurationMs = 0;
    private volatile int lastBatchSize = 0;

    @Autowired
    public LoadTestController(
            @Qualifier("tuningKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
            SlowConsumer slowConsumer) {
        this.kafkaTemplate = kafkaTemplate;
        this.slowConsumer = slowConsumer;
    }

    /**
     * Endpoint oficial según PDF 3 (Página 7):
     * GET /api/test/start?count=100000
     */
    @GetMapping("/start")
    public String startLoadTest(@RequestParam(defaultValue = "10000") int count) {
        long startTime = System.currentTimeMillis();

        // Generar carga masiva paralela hacia sensor-topic con 3 particiones y 5 keys
        IntStream.range(0, count).parallel().forEach(i -> {
            String message = "Sensor-Data-" + i + "-Payload-Voluminoso-Para-Llenar-Bytes";
            // Enviar sin esperar respuesta (Fire and Forget para máxima velocidad de prueba)
            kafkaTemplate.send("sensor-topic", "sensor-" + (i % 5), message);
        });

        long endTime = System.currentTimeMillis();
        long duration = Math.max(1, endTime - startTime);
        double throughput = (count / (duration / 1000.0));

        totalProducedCount.addAndGet(count);
        this.lastThroughput = throughput;
        this.lastDurationMs = duration;
        this.lastBatchSize = count;

        return String.format("🚀 Carga completada: %d mensajes enviados en %d ms. Throughput: %.2f msg/sec",
                count, duration, throughput);
    }

    /**
     * Endpoint con formato JSON para la interfaz gráfica React
     */
    @PostMapping("/load")
    public Map<String, Object> executeLoadTestJson(@RequestParam(defaultValue = "10000") int count) {
        long startTime = System.currentTimeMillis();

        IntStream.range(0, count).parallel().forEach(i -> {
            String message = "Sensor-Data-" + i + "-Payload-Voluminoso-Para-Llenar-Bytes-Timestamp=" + System.currentTimeMillis();
            kafkaTemplate.send("sensor-topic", "sensor-" + (i % 5), message);
        });

        long endTime = System.currentTimeMillis();
        long duration = Math.max(1, endTime - startTime);
        double throughput = (count / (duration / 1000.0));

        totalProducedCount.addAndGet(count);
        this.lastThroughput = throughput;
        this.lastDurationMs = duration;
        this.lastBatchSize = count;

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", count);
        result.put("durationMs", duration);
        result.put("throughputMsgSec", Math.round(throughput * 100.0) / 100.0);
        result.put("message", String.format("Carga de %d mensajes completada en %d ms (%.2f msg/seg)", count, duration, throughput));
        return result;
    }

    /**
     * Endpoint para consultar métricas en tiempo real de Consumer Lag
     * Fórmula: Lag = LogEndOffset (Producidos) - CurrentOffset (Consumidos)
     */
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        long produced = totalProducedCount.get();
        long consumed = slowConsumer.getProcessedCount();
        long lag = Math.max(0, produced - consumed);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("topic", "sensor-topic");
        metrics.put("consumerGroup", "analytics-group");
        metrics.put("partitions", 3);
        metrics.put("replicationFactor", 1);
        metrics.put("totalProduced", produced);
        metrics.put("totalConsumed", consumed);
        metrics.put("consumerLag", lag);
        metrics.put("consumerDelayMs", slowConsumer.getDelayMs());
        metrics.put("isConsumerPaused", slowConsumer.isPaused());
        metrics.put("lastThroughput", Math.round(lastThroughput * 100.0) / 100.0);
        metrics.put("lastDurationMs", lastDurationMs);
        metrics.put("lastBatchSize", lastBatchSize);

        // Configuración de tuning activa
        Map<String, Object> tuningConfig = new HashMap<>();
        tuningConfig.put("batchSize", "32 KB (32768 bytes)");
        tuningConfig.put("lingerMs", "20 ms");
        tuningConfig.put("compressionType", "snappy");
        tuningConfig.put("logRetentionMinutes", 5);
        tuningConfig.put("logRetentionBytes", "50 MB (52428800 bytes)");
        tuningConfig.put("logRetentionCheckIntervalMs", 60000);
        metrics.put("tuningSettings", tuningConfig);

        metrics.put("sampleRecords", slowConsumer.getSampleRecords());

        return metrics;
    }

    /**
     * Permite ajustar el retardo del consumidor en milisegundos desde la UI
     * (0ms para drenar el lag velozmente, 10ms estándar del PDF 3, 50ms para lag extremo)
     */
    @PostMapping("/consumer/delay")
    public Map<String, Object> setConsumerDelay(@RequestParam int delayMs) {
        slowConsumer.setDelayMs(delayMs);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("delayMs", slowConsumer.getDelayMs());
        res.put("message", "Retardo del consumidor actualizado a " + delayMs + " ms");
        return res;
    }

    /**
     * Pausar o reanudar el consumidor lento
     */
    @PostMapping("/consumer/pause")
    public Map<String, Object> togglePause(@RequestParam(required = false) Boolean pause) {
        boolean newState = pause != null ? pause : !slowConsumer.isPaused();
        slowConsumer.setPaused(newState);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("paused", newState);
        res.put("message", newState ? "Consumidor pausado (el lag crecerá estáticamente)" : "Consumidor reanudado");
        return res;
    }

    /**
     * Reiniciar contadores de prueba
     */
    @PostMapping("/reset")
    public Map<String, Object> resetAll() {
        totalProducedCount.set(0);
        lastThroughput = 0.0;
        lastDurationMs = 0;
        lastBatchSize = 0;
        slowConsumer.reset();
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Contadores de métricas de carga y Consumer Lag reiniciados.");
        return res;
    }
}
