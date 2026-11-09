package com.telemedicina.service;

import com.telemedicina.dto.AlertaClinicaDTO;
import com.telemedicina.dto.CitaEventoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CitasEventConsumer — Consumidor Kafka del tópico {@code telemedicina.citas.eventos}
 * con {@code @KafkaListener} y consumer group {@code telemedicina-citas-group}.
 *
 * Responsabilidades:
 *  1. Validar el payload (eventId, eventType, citaId, rutPaciente...).
 *  2. Auditar los eventos recibidos (lista en memoria para el frontend).
 *  3. Detectar condiciones de negocio que generan una alerta clínica
 *     (cita cancelada o reprogramada) y publicarla en el tópico de alertas
 *     usando la especialidad médica como clave.
 */
@Service
public class CitasEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CitasEventConsumer.class);

    private final List<CitaEventoDTO> receivedEvents = new CopyOnWriteArrayList<>();
    private final AlertaProducer alertaProducer;

    @Autowired
    public CitasEventConsumer(AlertaProducer alertaProducer) {
        this.alertaProducer = alertaProducer;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.citas-eventos.name:${app.kafka.topics.citas-eventos:telemedicina.citas.eventos}}",
            groupId = "telemedicina-citas-group",
            containerFactory = "telemedicinaCitasKafkaListenerContainerFactory"
    )
    public void listen(
            @Payload CitaEventoDTO evento,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[KAFKA CONSUMIDOR CITAS] Evento recibido eventType='{}' citaId='{}' especialidad='{}' partition={} offset={}",
                evento.getEventType(), evento.getCitaId(), evento.getEspecialidad(), partition, offset);

        // Validación de payload de negocio (eventos inválidos -> DLT)
        validarPayload(evento);

        receivedEvents.add(0, evento);
        if (receivedEvents.size() > 100) {
            receivedEvents.remove(receivedEvents.size() - 1);
        }

        // Detección de alertas clínicas a partir del evento
        procesarEvento(evento).ifPresent(alertaProducer::enviarAlerta);
    }

    /**
     * Lógica de negocio separada de la infraestructura de mensajería:
     * decide si un evento de cita debe generar una alerta clínica.
     * Retorna {@code Optional.empty()} cuando no corresponde alerta.
     */
    public Optional<AlertaClinicaDTO> procesarEvento(CitaEventoDTO evento) {
        if (CitaEventoDTO.EVENTO_CANCELADA.equals(evento.getEventType())) {
            return Optional.of(AlertaClinicaDTO.builder()
                    .alertaId(UUID.randomUUID().toString())
                    .tipo(AlertaClinicaDTO.TIPO_CANCELADA)
                    .severidad(AlertaClinicaDTO.SEVERIDAD_ALTA)
                    .rutPaciente(evento.getRutPaciente())
                    .nombrePaciente(evento.getNombrePaciente())
                    .citaId(evento.getCitaId())
                    .especialidad(evento.getEspecialidad())
                    .mensaje("Cita cancelada para paciente " + evento.getRutPaciente() + " - se requiere reagendar.")
                    .sourceEventId(evento.getEventId())
                    .timestamp(Instant.now().toString())
                    .build());
        }

        if (CitaEventoDTO.EVENTO_REPROGRAMADA.equals(evento.getEventType())) {
            return Optional.of(AlertaClinicaDTO.builder()
                    .alertaId(UUID.randomUUID().toString())
                    .tipo(AlertaClinicaDTO.TIPO_REPROGRAMADA)
                    .severidad(AlertaClinicaDTO.SEVERIDAD_MEDIA)
                    .rutPaciente(evento.getRutPaciente())
                    .nombrePaciente(evento.getNombrePaciente())
                    .citaId(evento.getCitaId())
                    .especialidad(evento.getEspecialidad())
                    .mensaje("Cita reprogramada para paciente " + evento.getRutPaciente()
                            + " (nueva fecha: " + evento.getFechaHora() + ").")
                    .sourceEventId(evento.getEventId())
                    .timestamp(Instant.now().toString())
                    .build());
        }

        return Optional.empty();
    }

    /**
     * Validación de payload de negocio. Lanza IllegalArgumentException si el
     * evento es inválido; el DefaultErrorHandler lo deriva al Dead Letter Topic.
     */
    private void validarPayload(CitaEventoDTO evento) {
        if (evento == null) {
            throw new IllegalArgumentException("Payload nulo recibido en telemedicina.citas.eventos");
        }
        if (evento.getEventId() == null || evento.getEventId().isBlank()) {
            throw new IllegalArgumentException("eventId es obligatorio");
        }
        if (evento.getEventType() == null || evento.getEventType().isBlank()) {
            throw new IllegalArgumentException("eventType es obligatorio");
        }
        if (evento.getCitaId() == null) {
            throw new IllegalArgumentException("citaId es obligatorio");
        }
        if (evento.getRutPaciente() == null || evento.getRutPaciente().isBlank()) {
            throw new IllegalArgumentException("rutPaciente es obligatorio");
        }
    }

    public List<CitaEventoDTO> getReceivedEvents() {
        return receivedEvents;
    }

    public void clearEvents() {
        receivedEvents.clear();
    }
}
