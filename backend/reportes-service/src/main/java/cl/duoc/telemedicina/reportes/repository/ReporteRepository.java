package cl.duoc.telemedicina.reportes.repository;

import cl.duoc.telemedicina.reportes.entity.ReporteOperativo;
import cl.duoc.telemedicina.reportes.entity.ReporteOperativo.Severidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<ReporteOperativo, Long> {
    List<ReporteOperativo> findByModuloServicio(String moduloServicio);
    List<ReporteOperativo> findBySeveridad(Severidad severidad);
}
