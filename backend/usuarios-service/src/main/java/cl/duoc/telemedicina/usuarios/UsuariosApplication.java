package cl.duoc.telemedicina.usuarios;

import cl.duoc.telemedicina.usuarios.entity.Medico;
import cl.duoc.telemedicina.usuarios.entity.Paciente;
import cl.duoc.telemedicina.usuarios.repository.MedicoRepository;
import cl.duoc.telemedicina.usuarios.repository.PacienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class UsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuariosApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(PacienteRepository pacienteRepo, MedicoRepository medicoRepo) {
        return args -> {
            if (pacienteRepo.count() == 0) {
                pacienteRepo.save(Paciente.builder()
                        .rut("18.452.123-9")
                        .nombres("Juan")
                        .apellidoPaterno("Pérez")
                        .apellidoMaterno("Garcés")
                        .fechaNacimiento(LocalDate.of(1985, 6, 14))
                        .telefonoContacto("+56912345678")
                        .email("juan.perez@gmail.com")
                        .direccionRural("Camino El Oasis S/N, San Pedro de Atacama")
                        .clinicaAsignada("Clínica Rural San Pedro de Atacama")
                        .build());
            }

            if (medicoRepo.count() == 0) {
                medicoRepo.save(Medico.builder()
                        .rut("12.345.678-K")
                        .nombreCompleto("Dr. Roberto Silva")
                        .especialidad("Cardiología")
                        .registroSuperintendencia("349281")
                        .email("roberto.silva@telemedicina.cl")
                        .telefono("+56998765432")
                        .esVoluntarioActivo(true)
                        .ciudadBase("Santiago")
                        .build());

                medicoRepo.save(Medico.builder()
                        .rut("14.987.654-3")
                        .nombreCompleto("Dra. Claudia Fuentealba")
                        .especialidad("Dermatología")
                        .registroSuperintendencia("582190")
                        .email("claudia.fuentealba@telemedicina.cl")
                        .telefono("+56987654321")
                        .esVoluntarioActivo(true)
                        .ciudadBase("Viña del Mar")
                        .build());
            }
        };
    }
}
