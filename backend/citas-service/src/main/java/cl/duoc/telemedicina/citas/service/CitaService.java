package cl.duoc.telemedicina.citas.service;

import cl.duoc.telemedicina.citas.entity.Cita;
import cl.duoc.telemedicina.citas.entity.Cita.EstadoCita;
import cl.duoc.telemedicina.citas.repository.CitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la gestión de citas de telemedicina.
 * Consume CitaEventProducer para desacoplar asíncronamente las notificaciones a RabbitMQ.
 */
@Service
public class CitaService {

    private static final Logger log = LoggerFactory.getLogger(CitaService.class);

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private CitaEventProducer citaEventProducer;

    public CitaService() {
    }

    public CitaService(CitaRepository citaRepository, CitaEventProducer citaEventProducer) {
        this.citaRepository = citaRepository;
        this.citaEventProducer = citaEventProducer;
    }

    public List<Cita> obtenerTodas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> obtenerPorId(Long id) {
        return citaRepository.findById(id);
    }

    public List<Cita> obtenerPorPaciente(String rutPaciente) {
        return citaRepository.findByRutPaciente(rutPaciente);
    }

    public List<Cita> obtenerPorMedico(String rutMedico) {
        return citaRepository.findByRutMedico(rutMedico);
    }

    public Cita crearCita(Cita cita) {
        Cita guardada = citaRepository.save(cita);
        log.info("Cita ID {} creada con éxito. Disparando evento asíncrono RabbitMQ...", guardada.getId());
        citaEventProducer.notificarCitaCreada(guardada);
        return guardada;
    }

    public Cita confirmarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        cita.setEstado(EstadoCita.CONFIRMADA);
        Cita guardada = citaRepository.save(cita);
        citaEventProducer.notificarCambioEstado(guardada, "CONFIRMADA");
        return guardada;
    }

    public Cita cancelarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        cita.setEstado(EstadoCita.CANCELADA);
        Cita guardada = citaRepository.save(cita);
        citaEventProducer.notificarCambioEstado(guardada, "CANCELADA");
        return guardada;
    }

    public Cita reprogramarCita(Long id, LocalDateTime nuevaFechaHora) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        cita.setFechaHora(nuevaFechaHora);
        cita.setEstado(EstadoCita.REPROGRAMADA);
        Cita guardada = citaRepository.save(cita);
        citaEventProducer.notificarCambioEstado(guardada, "REPROGRAMADA");
        return guardada;
    }
}
