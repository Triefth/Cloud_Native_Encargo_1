package cl.duoc.telemedicina.usuarios.repository;

import cl.duoc.telemedicina.usuarios.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByRut(String rut);
}
