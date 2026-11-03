package cl.duoc.telemedicina.notificaciones.service;

import cl.duoc.telemedicina.notificaciones.dto.AlertaClinicaDTO;
import cl.duoc.telemedicina.notificaciones.dto.CitaEventoDTO;
import cl.duoc.telemedicina.notificaciones.entity.Notificacion;
import cl.duoc.telemedicina.notificaciones.repository.NotificacionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Consumidor reactivo de Apache Kafka para notificaciones-service.
 *
 * Escucha dos tópicos centrales de telemedicina rural:
 * 1. {@code telemedicina.alertas}: Despacha SMS/Email urgentes ante cancelaciones o reprogramaciones.
 * 2. {@code telemedicina.citas.eventos}: Despacha recordatorios automáticos Anti No-Show ante citas creadas.
 */
@Service
public class AlertaKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertaKafkaConsumer.class);

    private final NotificacionRepository notificacionRepository;
    private final NotificacionKafkaProducer notificacionKafkaProducer;
    private final ObjectMapper objectMapper;

    @Autowired
    public AlertaKafkaConsumer(NotificacionRepository notificacionRepository,
                               @Autowired(required = false) NotificacionKafkaProducer notificacionKafkaProducer) {
        this.notificacionRepository = notificacionRepository;
        this.notificacionKafkaProducer = notificacionKafkaProducer;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Consume alertas clínicas emitidas por kafka-service (Caso de Uso 2 EP3).
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.alertas:telemedicina.alertas}",
            groupId = "notificaciones-alertas-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void recibirAlertaClinica(ConsumerRecord<String, Object> record) {
        try {
            AlertaClinicaDTO alerta = mapPayload(record.value(), AlertaClinicaDTO.class);
            if (alerta == null || alerta.getRutPaciente() == null) {
                log.warn("[KAFKA - NOTIFICACIONES] Alerta descartada por payload incompleto: {}", record.value());
                return;
            }

            log.info("🚨 [KAFKA - NOTIFICACIONES] Alerta clínica recibida [ID: {} | Severidad: {} | Paciente: {}]: {}",
                    alerta.getAlertaId(), alerta.getSeveridad(), alerta.getRutPaciente(), alerta.getMensaje());

            // Canal sugerido: SMS prioritario para zonas rurales
            Notificacion.TipoNotificacion canal = Notificacion.TipoNotificacion.SMS;
            if (AlertaClinicaDTO.SEVERIDAD_ALTA.equalsIgnoreCase(alerta.getSeveridad())) {
                canal = Notificacion.TipoNotificacion.SMS;
            } else if (AlertaClinicaDTO.SEVERIDAD_MEDIA.equalsIgnoreCase(alerta.getSeveridad())) {
                canal = Notificacion.TipoNotificacion.WHATSAPP;
            }

            Notificacion notif = Notificacion.builder()
                    .citaId(alerta.getCitaId())
                    .rutPaciente(alerta.getRutPaciente())
                    .tipo(canal)
                    .mensaje(String.format("Alerta Telemedicina [%s]: %s", alerta.getSeveridad(), alerta.getMensaje()))
                    .estado(Notificacion.EstadoEnvio.ENVIADO)
                    .confirmacionLectura(false)
                    .fechaEnvio(LocalDateTime.now())
                    .build();

            Notificacion guardada = notificacionRepository.save(notif);
            log.info("✅ [KAFKA - NOTIFICACIONES] Notificación #{} registrada en BD para paciente {}", guardada.getId(), guardada.getRutPaciente());

            // Publicar evento en telemedicina.notificaciones
            if (notificacionKafkaProducer != null) {
                notificacionKafkaProducer.publicarNotificacion(guardada, "ALERTA_KAFKA");
            }

        } catch (Exception e) {
            log.error("❌ [KAFKA - NOTIFICACIONES] Error al procesar alerta clínica de Kafka: {}", e.getMessage(), e);
        }
    }

    /**
     * Consume eventos del ciclo de vida de citas (Caso de Uso 1 EP3).
     * Despacha recordatorio automático Anti No-Show ante citas creadas.
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.citas-eventos:telemedicina.citas.eventos}",
            groupId = "notificaciones-citas-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void recibirEventoCita(ConsumerRecord<String, Object> record) {
        try {
            CitaEventoDTO evento = mapPayload(record.value(), CitaEventoDTO.class);
            if (evento == null || evento.getEventType() == null) {
                return;
            }

            // Despachar recordatorio al crear una nueva cita
            if (CitaEventoDTO.EVENTO_CREADA.equalsIgnoreCase(evento.getEventType())) {
                log.info("📅 [KAFKA - NOTIFICACIONES] Nueva cita #{} detectada para paciente {}. Programando recordatorio Anti No-Show.",
                        evento.getCitaId(), evento.getRutPaciente());

                String nombreMed = evento.getNombreMedico() != null ? evento.getNombreMedico() : "Médico de Turno";
                String fechaStr = evento.getFechaHora() != null ? evento.getFechaHora() : "próximamente";

                Notificacion notif = Notificacion.builder()
                        .citaId(evento.getCitaId())
                        .rutPaciente(evento.getRutPaciente())
                        .tipo(Notificacion.TipoNotificacion.SMS)
                        .mensaje(String.format("Recordatorio Anti No-Show: Su cita médica con %s está programada para %s. Responda SI para confirmar.",
                                nombreMed, fechaStr))
                        .estado(Notificacion.EstadoEnvio.ENVIADO)
                        .confirmacionLectura(false)
                        .fechaEnvio(LocalDateTime.now())
                        .build();

                Notificacion guardada = notificacionRepository.save(notif);
                log.info("📲 [KAFKA - NOTIFICACIONES] Recordatorio Anti No-Show registrado #{} para cita #{}", guardada.getId(), evento.getCitaId());

                // Publicar evento en telemedicina.notificaciones
                if (notificacionKafkaProducer != null) {
                    notificacionKafkaProducer.publicarNotificacion(guardada, "RECORDATORIO_AUTO");
                }
            }

        } catch (Exception e) {
            log.error("❌ [KAFKA - NOTIFICACIONES] Error al procesar evento de cita de Kafka: {}", e.getMessage(), e);
        }
    }

    private <T> T mapPayload(Object rawValue, Class<T> targetClass) {
        if (rawValue == null) return null;
        if (targetClass.isInstance(rawValue)) {
            return targetClass.cast(rawValue);
        }
        try {
            if (rawValue instanceof String) {
                return objectMapper.readValue((String) rawValue, targetClass);
            }
            return objectMapper.convertValue(rawValue, targetClass);
        } catch (Exception e) {
            log.warn("Fallo al deserializar payload hacia {}: {}", targetClass.getSimpleName(), e.getMessage());
            return null;
        }
    }
}
