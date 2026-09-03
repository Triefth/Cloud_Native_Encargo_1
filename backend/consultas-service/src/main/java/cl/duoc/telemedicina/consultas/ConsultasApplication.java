package cl.duoc.telemedicina.consultas;

import cl.duoc.telemedicina.consultas.entity.Consulta;
import cl.duoc.telemedicina.consultas.entity.Consulta.EstadoConsulta;
import cl.duoc.telemedicina.consultas.repository.ConsultaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class ConsultasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsultasApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(ConsultaRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(Consulta.builder()
                        .citaId(1L)
                        .rutPaciente("18.452.123-9")
                        .rutMedico("12.345.678-K")
                        .roomName("hipaa-cpaas-room-abc12345")
                        .cpaasJoinUrl("https://meet.telemedicina-rural.cl/v1/call/hipaa-cpaas-room-abc12345?hipaa=true")
                        .estado(EstadoConsulta.FINALIZADA)
                        .diagnosticoPreliminar("Hipertensión arterial leve en control.")
                        .indicacionesMedicas("Mantener Enalapril 10mg cada 12 hrs. Reducir consumo de sodio.")
                        .duracionMinutos(20)
                        .fechaInicio(LocalDateTime.now().minusHours(2))
                        .fechaFin(LocalDateTime.now().minusHours(2).plusMinutes(20))
                        .build());
            }
        };
    }
}
