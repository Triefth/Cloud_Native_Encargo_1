package com.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    /**
     * Topic de la Guía 2: 3 particiones y factor de replicación 1
     */
    @Bean
    public NewTopic tutorialParticionesTopic() {
        return TopicBuilder.name("tutorial-particiones")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic de entrada para Kafka Streams (Guía 1)
     */
    @Bean
    public NewTopic bancoTransaccionesTopic() {
        return TopicBuilder.name("banco-transacciones")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic de salida para alertas de fraude (Guía 1)
     */
    @Bean
    public NewTopic bancoAlertasTopic() {
        return TopicBuilder.name("banco-alertas")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic de IoT para Guía de Tuning, Pruebas de Carga y Consumer Lag (PDF 2 y PDF 3)
     * Configurado con 3 particiones para permitir paralelismo de consumo.
     */
    @Bean
    public NewTopic sensorTopic() {
        return TopicBuilder.name("sensor-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic principal para demostración de Validación de Payloads, Reintentos e Idempotencia (PDF 1)
     */
    @Bean
    public NewTopic bancoResilienteTopic() {
        return TopicBuilder.name("banco-resiliente")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Dead Letter Topic (DLT) para mensajes que agotaron reintentos o fallaron permanentemente (PDF 1)
     */
    @Bean
    public NewTopic bancoResilienteDltTopic() {
        return TopicBuilder.name("banco-resiliente.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
