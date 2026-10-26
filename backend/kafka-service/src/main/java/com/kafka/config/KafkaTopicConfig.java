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
}
