package cl.duoc.telemedicina.notificaciones.repository;

import cl.duoc.telemedicina.notificaciones.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByRutPaciente(String rutPaciente);
    List<Notificacion> findByCitaId(Long citaId);
}
