package cl.duoc.telemedicina.citas.config;

import cl.duoc.telemedicina.citas.dto.CitaEventoDTO;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaProducerConfig — Configuración del cliente productor Kafka de citas-service.
 *
 * Cumple con Pauta Duoc UC EP3:
 *  - Centralización de propiedades de tópicos en application.yml evitando cadenas hardcoded.
 *  - Declaración explícita de beans NewTopic con TopicBuilder (3 particiones, 1 réplica).
 *  - Publica eventos JSON (CitaEventoDTO) con idempotencia (acks=all, retries) para entrega confiable.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.citas-eventos.name:${app.kafka.topic.citas-eventos:telemedicina.citas.eventos}}")
    private String topicCitasEventos;

    @Value("${app.kafka.topics.citas-eventos.partitions:3}")
    private int citasEventosPartitions;

    @Value("${app.kafka.topics.citas-eventos.replicas:1}")
    private int citasEventosReplicas;

    /**
     * Declara explícitamente el tópico en Kafka con 3 particiones y 1 réplica al arrancar Spring.
     */
    @Bean
    public NewTopic citasEventosTopic() {
        return TopicBuilder.name(topicCitasEventos)
                .partitions(citasEventosPartitions)
                .replicas(citasEventosReplicas)
                .build();
    }

    @Bean
    public ProducerFactory<String, CitaEventoDTO> citaEventoProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // JSON portable sin cabeceras de tipo
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        // Idempotencia + entrega confiable
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, CitaEventoDTO> citaEventoKafkaTemplate() {
        return new KafkaTemplate<>(citaEventoProducerFactory());
    }
}
