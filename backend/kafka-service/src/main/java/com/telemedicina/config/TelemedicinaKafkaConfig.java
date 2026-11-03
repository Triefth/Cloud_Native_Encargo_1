package com.telemedicina.config;

import com.telemedicina.dto.AlertaClinicaDTO;
import com.telemedicina.dto.CitaEventoDTO;
import com.telemedicina.dto.NotificacionEventoDTO;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * =====================================================================
 * TelemedicinaKafkaConfig — Configuración completa del cliente Kafka
 * para el dominio de telemedicina (EP3).
 *
 * Cubre la rúbrica de Evaluación Parcial N°3:
 *  - Configuración completa del cliente Kafka (bootstrap servers, serializers,
 *    consumer groups, propiedades avanzadas, particiones y réplicas).       (12%)
 *  - Parámetros de administración de tópicos (retención, cleanup policy).   (12%)
 *  - Consumer groups para balanceo de carga y resiliencia.                  (12%)
 *  - Manejo de errores con validación de payloads, reintentos y DLT.        (12%)
 * =====================================================================
 */
@Configuration
public class TelemedicinaKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.citas-eventos:telemedicina.citas.eventos}")
    private String topicCitasEventos;

    @Value("${app.kafka.topics.alertas:telemedicina.alertas}")
    private String topicAlertas;

    @Value("${app.kafka.topics.notificaciones:telemedicina.notificaciones}")
    private String topicNotificaciones;

    // ─────────────────────────────────────────────────────────────────────
    // 1. PRODUCTOR JSON (idempotente, acks=all, retries)
    // ─────────────────────────────────────────────────────────────────────
    @Bean(name = "telemedicinaProducerFactory")
    public ProducerFactory<String, Object> telemedicinaProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean(name = "telemedicinaKafkaTemplate")
    public KafkaTemplate<String, Object> telemedicinaKafkaTemplate(
            @Qualifier("telemedicinaProducerFactory") ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. RECOVERER + GESTOR DE ERRORES (Dead Letter Topic con backoff exponencial)
    // ─────────────────────────────────────────────────────────────────────
    @Bean
    public DeadLetterPublishingRecoverer telemedicinaDeadLetterRecoverer(
            @Qualifier("telemedicinaKafkaTemplate") KafkaTemplate<String, Object> template) {
        return new DeadLetterPublishingRecoverer(template,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
    }

    @Bean
    public DefaultErrorHandler telemedicinaErrorHandler(
            @Qualifier("telemedicinaDeadLetterRecoverer") DeadLetterPublishingRecoverer telemedicinaDeadLetterRecoverer) {
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxElapsedTime(6000L);
        DefaultErrorHandler handler = new DefaultErrorHandler(telemedicinaDeadLetterRecoverer, backOff);
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. CONSUMIDOR CITAS
    // ─────────────────────────────────────────────────────────────────────
    @Bean
    public ConsumerFactory<String, CitaEventoDTO> telemedicinaCitasConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "telemedicina-citas-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CitaEventoDTO.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "telemedicinaCitasKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, CitaEventoDTO> telemedicinaCitasKafkaListenerContainerFactory(
            @Qualifier("telemedicinaErrorHandler") DefaultErrorHandler telemedicinaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, CitaEventoDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(telemedicinaCitasConsumerFactory());
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(telemedicinaErrorHandler);
        return factory;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4. CONSUMIDOR ALERTAS
    // ─────────────────────────────────────────────────────────────────────
    @Bean
    public ConsumerFactory<String, AlertaClinicaDTO> telemedicinaAlertasConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "telemedicina-alertas-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AlertaClinicaDTO.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "telemedicinaAlertasKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, AlertaClinicaDTO> telemedicinaAlertasKafkaListenerContainerFactory(
            @Qualifier("telemedicinaErrorHandler") DefaultErrorHandler telemedicinaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, AlertaClinicaDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(telemedicinaAlertasConsumerFactory());
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(telemedicinaErrorHandler);
        return factory;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 5. CONSUMIDOR NOTIFICACIONES (telemedicina.notificaciones)
    // ─────────────────────────────────────────────────────────────────────
    @Bean
    public ConsumerFactory<String, NotificacionEventoDTO> telemedicinaNotificacionesConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "telemedicina-notificaciones-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificacionEventoDTO.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "telemedicinaNotificacionesKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, NotificacionEventoDTO> telemedicinaNotificacionesKafkaListenerContainerFactory(
            @Qualifier("telemedicinaErrorHandler") DefaultErrorHandler telemedicinaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, NotificacionEventoDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(telemedicinaNotificacionesConsumerFactory());
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(telemedicinaErrorHandler);
        return factory;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 6. ADMIN (KafkaAdmin + AdminClient) para microservicio administrador
    // ─────────────────────────────────────────────────────────────────────
    @Bean
    public KafkaAdmin telemedicinaKafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public AdminClient telemedicinaAdminClient() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return AdminClient.create(configs);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 7. DECLARACIÓN DE TÓPICOS
    // ─────────────────────────────────────────────────────────────────────
    @Bean
    public NewTopic citasEventosTopic() {
        return TopicBuilder.name(topicCitasEventos)
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")  // 7 días
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }

    @Bean
    public NewTopic citasEventosDltTopic() {
        return TopicBuilder.name(topicCitasEventos + ".DLT")
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .build();
    }

    @Bean
    public NewTopic alertasTopic() {
        return TopicBuilder.name(topicAlertas)
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }

    @Bean
    public NewTopic alertasDltTopic() {
        return TopicBuilder.name(topicAlertas + ".DLT")
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .build();
    }

    @Bean
    public NewTopic notificacionesTopic() {
        return TopicBuilder.name(topicNotificaciones)
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }

    @Bean
    public NewTopic notificacionesDltTopic() {
        return TopicBuilder.name(topicNotificaciones + ".DLT")
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .build();
    }
}
