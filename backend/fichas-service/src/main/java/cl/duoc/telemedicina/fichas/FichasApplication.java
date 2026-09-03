package cl.duoc.telemedicina.fichas;

import cl.duoc.telemedicina.fichas.entity.FichaMedica;
import cl.duoc.telemedicina.fichas.repository.FichaRepository;
import cl.duoc.telemedicina.fichas.service.FichaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FichasApplication {

    public static void main(String[] args) {
        SpringApplication.run(FichasApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(FichaRepository repository, FichaService fichaService) {
        return args -> {
            if (repository.count() == 0) {
                FichaMedica f1 = repository.save(FichaMedica.builder()
                        .rutPaciente("18.452.123-9")
                        .nombreCompleto("Juan Pérez Garcés")
                        .fechaNacimiento("1985-06-14")
                        .grupoSanguineo("O+")
                        .alergias("Penicilina")
                        .enfermedadesCronicas("Hipertensión Arterial")
                        .clinicaOrigen("Clínica Rural San Pedro de Atacama")
                        .build());

                fichaService.registrarAtencionRemota(
                        "18.452.123-9",
                        1L,
                        "Dr. Roberto Silva",
                        "Cardiología",
                        "Teleconsulta de control. Paciente refiere buena tolerancia a medicamentos."
                );
            }
        };
    }
}
