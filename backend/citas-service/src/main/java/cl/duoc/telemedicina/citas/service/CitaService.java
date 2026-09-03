package cl.duoc.telemedicina.citas.service;

import cl.duoc.telemedicina.citas.entity.Cita;
import cl.duoc.telemedicina.citas.entity.Cita.EstadoCita;
import cl.duoc.telemedicina.citas.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

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
        return citaRepository.save(cita);
    }

    public Cita confirmarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        cita.setEstado(EstadoCita.CONFIRMADA);
        return citaRepository.save(cita);
    }

    public Cita cancelarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    public Cita reprogramarCita(Long id, LocalDateTime nuevaFechaHora) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        cita.setFechaHora(nuevaFechaHora);
        cita.setEstado(EstadoCita.REPROGRAMADA);
        return citaRepository.save(cita);
    }
}
