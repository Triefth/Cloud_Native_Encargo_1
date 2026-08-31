package cl.duoc.telemedicina.fichas.repository;

import cl.duoc.telemedicina.fichas.entity.FichaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FichaRepository extends JpaRepository<FichaMedica, Long> {
    Optional<FichaMedica> findByRutPaciente(String rutPaciente);
}
