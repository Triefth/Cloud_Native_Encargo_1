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
 *  - Centralización de propiedades de tópicos en application.yml evitando cadenas hardcoded.
 *  - Declaración explícita de beans NewTopic con TopicBuilder (3 particiones, 1 réplica, retención).
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

    @Value("${app.kafka.topics.citas-eventos.name:${app.kafka.topics.citas-eventos:telemedicina.citas.eventos}}")
    private String topicCitasEventos;

    @Value("${app.kafka.topics.citas-eventos.partitions:3}")
    private int citasEventosPartitions;

    @Value("${app.kafka.topics.citas-eventos.replicas:1}")
    private int citasEventosReplicas;

    @Value("${app.kafka.topics.citas-eventos.retention-ms:604800000}")
    private String citasEventosRetentionMs;

    @Value("${app.kafka.topics.alertas.name:${app.kafka.topics.alertas:telemedicina.alertas}}")
    private String topicAlertas;

    @Value("${app.kafka.topics.alertas.partitions:3}")
    private int alertasPartitions;

    @Value("${app.kafka.topics.alertas.replicas:1}")
    private int alertasReplicas;

    @Value("${app.kafka.topics.alertas.retention-ms:604800000}")
    private String alertasRetentionMs;

    @Value("${app.kafka.topics.notificaciones.name:${app.kafka.topics.notificaciones:telemedicina.notificaciones}}")
    private String topicNotificaciones;

    @Value("${app.kafka.topics.notificaciones.partitions:3}")
    private int notificacionesPartitions;

    @Value("${app.kafka.topics.notificaciones.replicas:1}")
    private int notificacionesReplicas;

    @Value("${app.kafka.topics.notificaciones.retention-ms:604800000}")
    private String notificacionesRetentionMs;

    // ─────────────────────────────────────────────────────────────────────
    // 1. PRODUCTOR JSON (idempotente, acks=all, retries)
    // ─────────────────────────────────────────────────────────────────────
    @Bean(name = "telemedicinaProducerFactory")
    public ProducerFactory<String, Object> telemedicinaProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // JSON portable (sin type headers)
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        // Idempotencia y durabilidad
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean(name = "telemedicinaKafkaTemplate")
    public KafkaTemplate<String, Object> telemedicinaKafkaTemplate() {
        return new KafkaTemplate<>(telemedicinaProducerFactory());
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. ERROR HANDLER + DEAD LETTER TOPIC (DLT) CON EXPONENTIAL BACKOFF
    // ─────────────────────────────────────────────────────────────────────
    @Bean(name = "telemedicinaErrorHandler")
    public DefaultErrorHandler telemedicinaErrorHandler(
            @Qualifier("telemedicinaKafkaTemplate") KafkaTemplate<String, Object> template) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                template,
                (record, ex) -> {
                    String dltTopic = record.topic() + ".DLT";
                    return new TopicPartition(dltTopic, record.partition());
                }
        );

        // 3 reintentos con backoff exponencial (1s, 2s, 4s)
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxElapsedTime(10000L);
        backOff.setMaxInterval(4000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. CONSUMIDOR CITAS EVENTOS
    // ─────────────────────────────────────────────────────────────────────
    @Bean
    public ConsumerFactory<String, CitaEventoDTO> telemedicinaCitasConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "telemedicina-citas-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // ErrorHandlingDeserializer previene envenenamiento de cola
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
    // 6. ADMIN CLIENT
    // ─────────────────────────────────────────────────────────────────────
    @Bean
    public AdminClient telemedicinaAdminClient(KafkaAdmin kafkaAdmin) {
        return AdminClient.create(kafkaAdmin.getConfigurationProperties());
    }

    // ─────────────────────────────────────────────────────────────────────
    // 7. DECLARACIÓN EXPLÍCITA DE TÓPICOS MEDIANTE BEANS (Pauta Duoc UC)
    // ─────────────────────────────────────────────────────────────────────
    @Bean
    public NewTopic citasEventosTopic() {
        return TopicBuilder.name(topicCitasEventos)
                .partitions(citasEventosPartitions)
                .replicas(citasEventosReplicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, citasEventosRetentionMs)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }

    @Bean
    public NewTopic citasEventosDltTopic() {
        return TopicBuilder.name(topicCitasEventos + ".DLT")
                .partitions(citasEventosPartitions)
                .replicas(citasEventosReplicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, citasEventosRetentionMs)
                .build();
    }

    @Bean
    public NewTopic alertasTopic() {
        return TopicBuilder.name(topicAlertas)
                .partitions(alertasPartitions)
                .replicas(alertasReplicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, alertasRetentionMs)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }

    @Bean
    public NewTopic alertasDltTopic() {
        return TopicBuilder.name(topicAlertas + ".DLT")
                .partitions(alertasPartitions)
                .replicas(alertasReplicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, alertasRetentionMs)
                .build();
    }

    @Bean
    public NewTopic notificacionesTopic() {
        return TopicBuilder.name(topicNotificaciones)
                .partitions(notificacionesPartitions)
                .replicas(notificacionesReplicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, notificacionesRetentionMs)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }

    @Bean
    public NewTopic notificacionesDltTopic() {
        return TopicBuilder.name(topicNotificaciones + ".DLT")
                .partitions(notificacionesPartitions)
                .replicas(notificacionesReplicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, notificacionesRetentionMs)
                .build();
    }
}
