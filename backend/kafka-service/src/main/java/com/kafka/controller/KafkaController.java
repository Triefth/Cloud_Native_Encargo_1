package com.kafka.controller;

import com.kafka.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que envía mensajes a Kafka
 *
 * Demuestra dos conceptos:
 * - Sin Key: Round Robin (distribuye entre particiones 0, 1, 2...)
 * - Con Key: Sticky Partitioning (todos a la misma partición con orden garantizado)
 */
@RestController
@RequestMapping("/api/kafka")
@CrossOrigin(origins = "*")
public class KafkaController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaConsumer kafkaConsumer;

    @Autowired
    public KafkaController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Endpoint para enviar mensajes a Kafka vía GET (según Guía página 11)
     * Ejemplos:
     * - SIN KEY: GET /api/kafka/send?message=Mensaje1
     * - CON KEY: GET /api/kafka/send?message=Paso1-Creada&key=orden-555
     */
    @GetMapping("/send")
    public String sendMessageGet(
            @RequestParam String message,
            @RequestParam(required = false) String key) {
        return handleSend(message, key);
    }

    /**
     * Endpoint para enviar mensajes a Kafka vía POST (según Guía página 15)
     */
    @PostMapping("/send")
    public String sendMessagePost(
            @RequestParam String message,
            @RequestParam(required = false) String key) {
        return handleSend(message, key);
    }

    private String handleSend(String message, String key) {
        String topicName = "tutorial-particiones";

        if (key != null && !key.trim().isEmpty()) {
            // Con Key: Kafka calcula hash(key) para determinar partición
            // Todos los mensajes con la misma key van a la MISMA partición
            kafkaTemplate.send(topicName, key, message);
            return "Mensaje enviado al topic '" + topicName
                    + "' con KEY: " + key
                    + " | MENSAJE: " + message;
        } else {
            // Sin Key: Kafka usa Round Robin
            // Distribuye automáticamente entre particiones 0, 1, 2
            kafkaTemplate.send(topicName, message);
            return "Mensaje enviado al topic '" + topicName
                    + "' (sin key - distribución automática) | MENSAJE: " + message;
        }
    }

    /**
     * Endpoint de verificación (según Guía página 12 y 16)
     */
    @GetMapping("/health")
    public String health() {
        return "Aplicación de Kafka Tutorial está corriendo en puerto 8888";
    }

    /**
     * Endpoint auxiliar para consultar mensajes recibidos en vivo desde la UI
     */
    @GetMapping("/messages")
    public List<KafkaConsumer.ReceivedRecord> getMessages() {
        return kafkaConsumer.getReceivedRecords();
    }

    /**
     * Endpoint auxiliar para limpiar historial de mensajes de prueba
     */
    @DeleteMapping("/messages")
    public String clearMessages() {
        kafkaConsumer.clearRecords();
        return "Historial de mensajes limpiado";
    }
}
