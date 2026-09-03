package cl.duoc.telemedicina.citas;

import cl.duoc.telemedicina.citas.entity.Cita;
import cl.duoc.telemedicina.citas.entity.Cita.EstadoCita;
import cl.duoc.telemedicina.citas.repository.CitaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class CitasApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(CitaRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(Cita.builder()
                        .rutPaciente("18.452.123-9")
                        .nombrePaciente("Juan Pérez Garcés")
                        .rutMedico("12.345.678-K")
                        .nombreMedico("Dr. Roberto Silva (Cardiólogo)")
                        .especialidad("Cardiología")
                        .fechaHora(LocalDateTime.now().plusDays(2).withHour(10).withMinute(30))
                        .estado(EstadoCita.PROGRAMADA)
                        .motivoConsulta("Evaluación arritmia y control de presión")
                        .clinicaRural("Clínica Rural San Pedro de Atacama")
                        .build());

                repository.save(Cita.builder()
                        .rutPaciente("15.789.456-2")
                        .nombrePaciente("María Elena Morales")
                        .rutMedico("14.987.654-3")
                        .nombreMedico("Dra. Claudia Fuentealba (Dermatología)")
                        .especialidad("Dermatología")
                        .fechaHora(LocalDateTime.now().plusDays(3).withHour(15).withMinute(0))
                        .estado(EstadoCita.CONFIRMADA)
                        .motivoConsulta("Revisión de lesión cutánea")
                        .clinicaRural("Clínica Rural de Huara")
                        .build());
            }
        };
    }
}
