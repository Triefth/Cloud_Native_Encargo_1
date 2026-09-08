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
                repository.save(FichaMedica.builder()
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

                repository.save(FichaMedica.builder()
                        .rutPaciente("12.345.678-9")
                        .nombreCompleto("Juan Pérez Morales")
                        .fechaNacimiento("1990-03-22")
                        .grupoSanguineo("O+")
                        .alergias("Penicilina, Polvos silvestres")
                        .enfermedadesCronicas("Hipertensión Arterial Grado 1")
                        .clinicaOrigen("Posta Rural Petorca")
                        .build());

                fichaService.registrarAtencionRemota(
                        "12.345.678-9",
                        2L,
                        "Dr. Alejandro Silva",
                        "Medicina General",
                        "Ajuste de dosis Losartán 50mg diario."
                );
            }
        };
    }
}
