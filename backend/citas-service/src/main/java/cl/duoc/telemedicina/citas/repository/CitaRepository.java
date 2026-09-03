package cl.duoc.telemedicina.citas.repository;

import cl.duoc.telemedicina.citas.entity.Cita;
import cl.duoc.telemedicina.citas.entity.Cita.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByRutPaciente(String rutPaciente);
    List<Cita> findByRutMedico(String rutMedico);
    List<Cita> findByEstado(EstadoCita estado);
}
