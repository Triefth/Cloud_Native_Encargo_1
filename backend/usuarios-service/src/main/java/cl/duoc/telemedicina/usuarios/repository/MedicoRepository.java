package cl.duoc.telemedicina.usuarios.repository;

import cl.duoc.telemedicina.usuarios.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
    Optional<Medico> findByRut(String rut);
    List<Medico> findByEspecialidad(String especialidad);
    List<Medico> findByEsVoluntarioActivo(Boolean esVoluntarioActivo);
}
