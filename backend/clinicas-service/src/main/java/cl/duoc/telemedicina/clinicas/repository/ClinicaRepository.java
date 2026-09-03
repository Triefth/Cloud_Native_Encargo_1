package cl.duoc.telemedicina.clinicas.repository;

import cl.duoc.telemedicina.clinicas.entity.ClinicaRural;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicaRepository extends JpaRepository<ClinicaRural, Long> {
    Optional<ClinicaRural> findByNombre(String nombre);
}
