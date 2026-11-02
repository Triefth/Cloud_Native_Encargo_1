package com.telemedicina.controller;

import com.telemedicina.dto.AlertaClinicaDTO;
import com.telemedicina.dto.CitaEventoDTO;
import com.telemedicina.service.AlertasConsumer;
import com.telemedicina.service.CitasEventConsumer;
import com.telemedicina.service.CitasEventProducer;
import com.telemedicina.service.TelemedicinaAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * TelemedicinaKafkaController — API REST del dominio de telemedicina con Kafka (EP3).
 *
 * Expone:
 *  - Simulación de eventos de citas (productor).
 *  - Consulta de eventos y alertas consumidos en vivo (consumidores).
 *  - Administración de tópicos y consumer groups (microservicio administrador).
 */
@RestController
@RequestMapping("/api/telemedicina")
@CrossOrigin(origins = "*")
public class TelemedicinaKafkaController {

    private final CitasEventProducer citasEventProducer;
    private final CitasEventConsumer citasEventConsumer;
    private final AlertasConsumer alertasConsumer;
    private final TelemedicinaAdminService adminService;

    @Autowired
    public TelemedicinaKafkaController(CitasEventProducer citasEventProducer,
                                       CitasEventConsumer citasEventConsumer,
                                       AlertasConsumer alertasConsumer,
                                       TelemedicinaAdminService adminService) {
        this.citasEventProducer = citasEventProducer;
        this.citasEventConsumer = citasEventConsumer;
        this.alertasConsumer = alertasConsumer;
        this.adminService = adminService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", "UP");
        m.put("service", "telemedicina-kafka");
        m.put("casosDeUso", List.of(
                "Streaming del ciclo de vida de citas (telemedicina.citas.eventos)",
                "Alertas clinicas en tiempo real (telemedicina.alertas)"));
        return m;
    }

    // ─────────────────────────────────────────────────────────────────────
    // SIMULACIÓN / PRODUCCIÓN
    // ─────────────────────────────────────────────────────────────────────
    @PostMapping("/eventos/simular")
    public Map<String, Object> simularEvento(@RequestBody(required = false) CitaEventoDTO evento) {
        CitaEventoDTO ev = evento != null ? evento : CitaEventoDTO.builder()
                .eventType(CitaEventoDTO.EVENTO_CREADA)
                .estado("PROGRAMADA")
                .build();

        if (ev.getEventId() == null || ev.getEventId().isBlank()) {
            ev.setEventId(UUID.randomUUID().toString());
        }
        if (ev.getTimestamp() == null || ev.getTimestamp().isBlank()) {
            ev.setTimestamp(Instant.now().toString());
        }
        if (ev.getRutPaciente() == null || ev.getRutPaciente().isBlank()) {
            ev.setRutPaciente("12.345.678-9");
        }
        if (ev.getEventType() == null || ev.getEventType().isBlank()) {
            ev.setEventType(CitaEventoDTO.EVENTO_CREADA);
        }

        citasEventProducer.publicarEvento(ev);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("publicado", true);
        m.put("topic", "telemedicina.citas.eventos");
        m.put("evento", ev);
        return m;
    }

    // ─────────────────────────────────────────────────────────────────────
    // CONSULTA (eventos y alertas consumidos en vivo)
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/eventos")
    public List<CitaEventoDTO> getEventos() {
        return citasEventConsumer.getReceivedEvents();
    }

    @GetMapping("/alertas")
    public List<AlertaClinicaDTO> getAlertas() {
        return alertasConsumer.getReceivedAlertas();
    }

    @DeleteMapping("/eventos")
    public Map<String, Object> clearEventos() {
        citasEventConsumer.clearEvents();
        return Map.of("cleared", true, "collection", "eventos");
    }

    @DeleteMapping("/alertas")
    public Map<String, Object> clearAlertas() {
        alertasConsumer.clearAlertas();
        return Map.of("cleared", true, "collection", "alertas");
    }

    // ─────────────────────────────────────────────────────────────────────
    // ADMINISTRACIÓN DE TÓPICOS Y CONSUMER GROUPS
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/admin/topics")
    public Map<String, Object> listTopics() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("topics", adminService.listTopics());
        return m;
    }

    @GetMapping("/admin/topics/{name}")
    public Map<String, Object> describeTopic(@PathVariable String name) {
        return adminService.describeTopic(name);
    }

    @PostMapping("/admin/topics")
    public Map<String, Object> createTopic(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        int partitions = body.get("partitions") != null ? ((Number) body.get("partitions")).intValue() : 3;
        short replicas = body.get("replicas") != null ? ((Number) body.get("replicas")).shortValue() : 1;
        String retentionMs = (String) body.get("retentionMs");
        String cleanupPolicy = (String) body.get("cleanupPolicy");
        return adminService.createTopic(name, partitions, replicas, retentionMs, cleanupPolicy);
    }

    @DeleteMapping("/admin/topics/{name}")
    public Map<String, Object> deleteTopic(@PathVariable String name) {
        return adminService.deleteTopic(name);
    }

    @GetMapping("/admin/consumer-groups")
    public Map<String, Object> listConsumerGroups() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("consumerGroups", adminService.listConsumerGroups());
        return m;
    }

    @GetMapping("/admin/consumer-groups/{groupId}")
    public Map<String, Object> describeConsumerGroup(@PathVariable String groupId) {
        return adminService.describeConsumerGroup(groupId);
    }
}
