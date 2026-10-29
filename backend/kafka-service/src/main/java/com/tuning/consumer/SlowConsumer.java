package com.tuning.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Consumidor con Retardo Simulado para Demostración de Consumer Lag
 * Siguiendo la Guía de Tuning y Pruebas de Carga (Duoc UC 2025 - PDF 3).
 *
 * Para observar el Consumer Lag (diferencia entre LogEndOffset y CurrentOffset),
 * este consumidor simula un procesamiento pesado (ej. IA, procesamiento de imágenes o BD)
 * mediante un retardo controlado (10ms por defecto = máx 100 msg/s).
 */
@Service
public class SlowConsumer {

    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicInteger delayMs = new AtomicInteger(10); // 10ms por defecto
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicLong lastProcessedTime = new AtomicLong(0);
    private final List<ConsumerMetricRecord> sampleRecords = new CopyOnWriteArrayList<>();

    public static class ConsumerMetricRecord {
        private final String key;
        private final int partition;
        private final long offset;
        private final long timestamp;

        public ConsumerMetricRecord(String key, int partition, long offset, long timestamp) {
            this.key = key;
            this.partition = partition;
            this.offset = offset;
            this.timestamp = timestamp;
        }

        public String getKey() { return key; }
        public int getPartition() { return partition; }
        public long getOffset() { return offset; }
        public long getTimestamp() { return timestamp; }
    }

    @KafkaListener(topics = "sensor-topic", groupId = "analytics-group")
    public void listen(ConsumerRecord<String, String> record) {
        while (isPaused.get()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // Simulamos un procesamiento pesado (ej. escribir en BD, IA)
        int currentDelay = delayMs.get();
        if (currentDelay > 0) {
            try {
                Thread.sleep(currentDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        long count = processedCount.incrementAndGet();
        lastProcessedTime.set(System.currentTimeMillis());

        // Muestreo para el UI (guardar 1 de cada 100 mensajes o los últimos 20 para no saturar memoria)
        if (count % 50 == 0 || sampleRecords.size() < 20) {
            sampleRecords.add(0, new ConsumerMetricRecord(record.key(), record.partition(), record.offset(), System.currentTimeMillis()));
            if (sampleRecords.size() > 50) {
                sampleRecords.remove(sampleRecords.size() - 1);
            }
        }
    }

    public long getProcessedCount() {
        return processedCount.get();
    }

    public int getDelayMs() {
        return delayMs.get();
    }

    public void setDelayMs(int ms) {
        this.delayMs.set(Math.max(0, ms));
    }

    public boolean isPaused() {
        return isPaused.get();
    }

    public void setPaused(boolean paused) {
        this.isPaused.set(paused);
    }

    public long getLastProcessedTime() {
        return lastProcessedTime.get();
    }

    public List<ConsumerMetricRecord> getSampleRecords() {
        return sampleRecords;
    }

    public void reset() {
        processedCount.set(0);
        lastProcessedTime.set(0);
        sampleRecords.clear();
    }
}
