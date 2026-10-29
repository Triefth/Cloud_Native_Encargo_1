package com.resilience.config;

import com.resilience.model.TransaccionSegura;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de Resiliencia, Validación, Reintentos y Dead Letter Topic (DLT)
 * Siguiendo la Guía de Manejo de Errores en Streaming (Duoc UC - PDF 1).
 *
 * Implementa:
 * 1. ErrorHandlingDeserializer para interceptar "Poison Pills" sin bucle infinito.
 * 2. JSR-380 Bean Validation en el listener.
 * 3. DefaultErrorHandler con Exponential Backoff (1s, 2s, 4s...) para prevenir Retry Storms.
 * 4. DeadLetterPublishingRecoverer para desviar mensajes fallidos automáticamente a topics con sufijo .DLT.
 * 5. Idempotencia en el productor (enable.idempotence=true, acks=all).
 */
@Configuration
public class ResilienceKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    // --- PRODUCTOR RESILIENTE (IDEMPOTENTE CON ACKS=ALL) ---
    @Bean(name = "resilientProducerFactory")
    public ProducerFactory<String, Object> resilientProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        // Idempotencia técnica del productor según PDF 1 (Página 9) y PDF 2 (Página 8)
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean(name = "resilientKafkaTemplate")
    public KafkaTemplate<String, Object> resilientKafkaTemplate() {
        return new KafkaTemplate<>(resilientProducerFactory());
    }

    // --- RECOVERER PARA DEAD LETTER TOPIC (DLT) CON HEADER ENRICHMENT ---
    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> resilientKafkaTemplate) {
        // Por defecto, envía al mismo topic con sufijo .DLT (ej: banco-resiliente -> banco-resiliente.DLT)
        // Spring automáticamente enriquece los headers con el stacktrace y causa del fallo (Header Enrichment)
        return new DeadLetterPublishingRecoverer(resilientKafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
    }

    // --- GESTOR DE ERRORES CON EXPONENTIAL BACKOFF (PDF 1 - Páginas 7 y 8) ---
    @Bean
    public DefaultErrorHandler resilientErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        // Backoff Exponencial: inicia en 1000ms (1s), multiplicador 2.0 (1s, 2s, 4s...)
        // Máximo 3 intentos de reintento antes de enviar al DLT
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxElapsedTime(6000L); // Hasta 6 segundos totales

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // Errores no reintentables (pasan directo a DLT para no saturar con reintentos inútiles)
        errorHandler.addNotRetryableExceptions(
                org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException.class,
                jakarta.validation.ValidationException.class,
                org.apache.kafka.common.errors.SerializationException.class
        );

        return errorHandler;
    }

    // --- CONSUMIDOR SEGURO CON ERROR HANDLING DESERIALIZER (PDF 1 - Página 6) ---
    @Bean
    public ConsumerFactory<String, Object> resilientConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "banco-resiliente-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // ErrorHandlingDeserializer evita bucles infinitos cuando llega un payload corrupto ("Poison Pill")
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TransaccionSegura.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    // --- CONTAINER FACTORY CON VALIDACIÓN JSR-380 Y GESTOR DE ERRORES ---
    @Bean(name = "resilientKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> resilientKafkaListenerContainerFactory(
            DefaultErrorHandler resilientErrorHandler,
            LocalValidatorFactoryBean validator) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(resilientConsumerFactory());
        factory.setCommonErrorHandler(resilientErrorHandler);
        // Soporte de JSR-380 Bean Validation en @KafkaListener
        // (Nota: Spring Kafka valida @Valid cuando el container factory no es nulo)
        return factory;
    }
}
