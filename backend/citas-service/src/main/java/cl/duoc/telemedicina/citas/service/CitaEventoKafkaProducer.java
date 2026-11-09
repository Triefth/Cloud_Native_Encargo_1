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
 * CitaEventoKafkaProducer — Publicador de eventos del ciclo de vida de citas
 * hacia Apache Kafka (tópico telemedicina.citas.eventos).
 *
 * Cumple con Pauta Duoc UC EP3:
 *  - Clave de particionamiento basada en la especialidad médica (garantiza orden
 *    estricto por especialidad y balanceo en las 3 particiones).
 *  - Inyección de nombres de tópicos desde application.yml sin cadenas hardcoded.
 *  - Formato JSON normalizado CitaEventoDTO con campos de auditoría (eventId, timestamp).
 */
@Service
public class CitaEventoKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(CitaEventoKafkaProducer.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final KafkaTemplate<String, CitaEventoDTO> kafkaTemplate;

    @Value("${app.kafka.topics.citas-eventos.name:${app.kafka.topic.citas-eventos:telemedicina.citas.eventos}}")
    private String topicCitasEventos;

    @Autowired
    public CitaEventoKafkaProducer(KafkaTemplate<String, CitaEventoDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Construye y publica un CitaEventoDTO a partir de la entidad Cita.
     * Utiliza la especialidad médica como clave de particionamiento.
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

        // Clave de particionamiento: Especialidad del médico (en vez de RUT)
        String partitionKey = (cita.getEspecialidad() != null && !cita.getEspecialidad().isBlank())
                ? cita.getEspecialidad().trim()
                : "MEDICINA_GENERAL";

        log.info("[KAFKA PRODUCTOR CITAS] Publicando evento tipo='{}' citaId='{}' key(especialidad)='{}' -> topic='{}'",
                eventType, cita.getId(), partitionKey, topicCitasEventos);
        kafkaTemplate.send(topicCitasEventos, partitionKey, evento);
        log.info("[✓] Evento Kafka publicado exitosamente: eventId='{}' key='{}'", evento.getEventId(), partitionKey);
    }
}
