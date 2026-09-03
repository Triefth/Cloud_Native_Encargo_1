package cl.duoc.telemedicina.notificaciones.service;

import cl.duoc.telemedicina.notificaciones.entity.Notificacion;
import cl.duoc.telemedicina.notificaciones.entity.Notificacion.EstadoEnvio;
import cl.duoc.telemedicina.notificaciones.entity.Notificacion.TipoNotificacion;
import cl.duoc.telemedicina.notificaciones.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

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

        return notificacionRepository.save(notificacion);
    }

    public Notificacion marcarConfirmado(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada con ID: " + id));
        notificacion.setConfirmacionLectura(true);
        return notificacionRepository.save(notificacion);
    }
}
