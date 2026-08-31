package cl.duoc.telemedicina.consultas.repository;

import cl.duoc.telemedicina.consultas.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    Optional<Consulta> findByCitaId(Long citaId);
    List<Consulta> findByRutPaciente(String rutPaciente);
    List<Consulta> findByRutMedico(String rutMedico);
}
