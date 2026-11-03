package cl.duoc.telemedicina.notificaciones.service;

import cl.duoc.telemedicina.notificaciones.entity.Notificacion;
import cl.duoc.telemedicina.notificaciones.entity.Notificacion.EstadoEnvio;
import cl.duoc.telemedicina.notificaciones.entity.Notificacion.TipoNotificacion;
import cl.duoc.telemedicina.notificaciones.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired(required = false)
    private NotificacionKafkaProducer notificacionKafkaProducer;

    public List<Notificacion> obtenerTodas() {
        return notificacionRepository.findAll();
    }

    public List<Notificacion> obtenerPorPaciente(String rutPaciente) {
        return notificacionRepository.findByRutPaciente(rutPaciente);
    }

    public Notificacion enviarRecordatorio(Long citaId, String rutPaciente, TipoNotificacion tipo, String mensaje) {
        Notificacion notificacion = Notificacion.builder()
                .citaId(citaId)
                .rutPaciente(rutPaciente)
                .tipo(tipo != null ? tipo : TipoNotificacion.SMS)
                .mensaje(mensaje)
                .estado(EstadoEnvio.ENVIADO)
                .confirmacionLectura(false)
                .build();

        Notificacion guardada = notificacionRepository.save(notificacion);

        // Publicar en Apache Kafka para streaming y observabilidad
        if (notificacionKafkaProducer != null) {
            notificacionKafkaProducer.publicarNotificacion(guardada, "MANUAL_WEB");
        }

        return guardada;
    }

    public Notificacion marcarConfirmado(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada con ID: " + id));
        notificacion.setConfirmacionLectura(true);
        Notificacion actualizada = notificacionRepository.save(notificacion);

        // Notificar cambio de estado a Kafka
        if (notificacionKafkaProducer != null) {
            notificacionKafkaProducer.publicarNotificacion(actualizada, "CONFIRMACION_LECTURA");
        }

        return actualizada;
    }
}
