package cl.duoc.telemedicina.citas.service;

import cl.duoc.telemedicina.citas.dto.CitaEventoDTO;
import cl.duoc.telemedicina.citas.entity.Cita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * CitaEventoKafkaProducer — Productor Kafka de citas-service.
 *
 * Publica en el tópico {@code telemedicina.citas.eventos} el ciclo de vida de
 * las citas (creada, confirmada, cancelada, reprogramada), usando el RUT del
 * paciente como clave de partición para preservar el orden por paciente.
 */
@Service
public class CitaEventoKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(CitaEventoKafkaProducer.class);

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final KafkaTemplate<String, CitaEventoDTO> kafkaTemplate;

    @Value("${app.kafka.topic.citas-eventos:telemedicina.citas.eventos}")
    private String topicCitasEventos;

    @Autowired
    public CitaEventoKafkaProducer(KafkaTemplate<String, CitaEventoDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Construye y publica un CitaEventoDTO a partir de la entidad Cita.
     */
    public void publicarEvento(Cita cita, String eventType, String estado) {
        CitaEventoDTO evento = CitaEventoDTO.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .citaId(cita.getId())
                .rutPaciente(cita.getRutPaciente())
                .nombrePaciente(cita.getNombrePaciente())
                .rutMedico(cita.getRutMedico())
                .nombreMedico(cita.getNombreMedico())
                .especialidad(cita.getEspecialidad())
                .estado(estado)
                .fechaHora(cita.getFechaHora() != null ? cita.getFechaHora().format(ISO) : null)
                .motivoConsulta(cita.getMotivoConsulta())
                .clinicaRural(cita.getClinicaRural())
                .timestamp(Instant.now().toString())
                .build();

        log.info("[KAFKA PRODUCTOR CITAS] Publicando evento tipo='{}' citaId='{}' -> topic='{}'",
                eventType, cita.getId(), topicCitasEventos);
        kafkaTemplate.send(topicCitasEventos, cita.getRutPaciente(), evento);
        log.info("[✓] Evento Kafka publicado exitosamente: eventId='{}'", evento.getEventId());
    }
}
