package com.tuning.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de Tuning de Alto Rendimiento para el Productor Kafka
 * Siguiendo la Guía de Tuning y Pruebas de Carga (Duoc UC 2025 - PDF 3).
 *
 * Optimizaciones aplicadas:
 * 1. batch.size: Aumentado a 32KB (Default 16KB) -> Mayor empaquetamiento y menos llamadas I/O.
 * 2. linger.ms: Espera 20ms para llenar los batches antes de enviar.
 * 3. compression.type: "snappy" -> Excelente compresión con bajo costo de CPU.
 * 4. acks: "all" y enable.idempotence: true -> Garantía de entrega exactamente una vez en el broker.
 */
@Configuration
public class KafkaTuningConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean(name = "tuningProducerFactory")
    public ProducerFactory<String, String> tuningProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // --- TUNING DE ALTO RENDIMIENTO (GUÍA PDF 3) ---

        // 1. Batch Size: Aumentar a 32KB (Default 16KB)
        // Intentará agrupar mensajes hasta llegar a 32KB
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 32 * 1024);

        // 2. Linger MS: Esperar 20ms antes de enviar
        // Esto permite que se acumulen más mensajes en el batch reduciendo el costo de red
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 20);

        // 3. Compression: Snappy (Balance óptimo entre CPU y Red)
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        // 4. Idempotencia y Consistencia (PDF 1 y 2)
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean(name = "tuningKafkaTemplate")
    public KafkaTemplate<String, String> tuningKafkaTemplate() {
        return new KafkaTemplate<>(tuningProducerFactory());
    }
}
