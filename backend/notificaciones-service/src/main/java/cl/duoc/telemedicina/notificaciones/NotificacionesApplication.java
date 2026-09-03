package cl.duoc.telemedicina.notificaciones;

import cl.duoc.telemedicina.notificaciones.entity.Notificacion;
import cl.duoc.telemedicina.notificaciones.entity.Notificacion.TipoNotificacion;
import cl.duoc.telemedicina.notificaciones.repository.NotificacionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NotificacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacionesApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(NotificacionRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(Notificacion.builder()
                        .citaId(1L)
                        .rutPaciente("18.452.123-9")
                        .tipo(TipoNotificacion.SMS)
                        .mensaje("Recordatorio Telemedicina: Su cita con Dr. Roberto Silva es mañana a las 10:30 hrs. Responda 1 para confirmar.")
                        .confirmacionLectura(true)
                        .build());
            }
        };
    }
}
