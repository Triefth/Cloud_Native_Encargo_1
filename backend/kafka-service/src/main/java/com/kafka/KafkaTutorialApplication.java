package com.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Aplicación Spring Boot para tutorial de Kafka y Pipeline de Eventos en Tiempo Real
 * Demuestra:
 * 1. Topics, particiones y offsets (Sin key Round-Robin vs Con Key Sticky Partitioning)
 * 2. Kafka Streams Pipeline: Filtrado y Detección de Fraude en Tiempo Real
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.kafka", "com.banco"})
public class KafkaTutorialApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaTutorialApplication.class, args);
    }
}
