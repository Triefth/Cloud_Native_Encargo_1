package cl.duoc.telemedicina.reportes;

import cl.duoc.telemedicina.reportes.entity.ReporteOperativo;
import cl.duoc.telemedicina.reportes.entity.ReporteOperativo.Severidad;
import cl.duoc.telemedicina.reportes.repository.ReporteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ReportesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportesApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(ReporteRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(ReporteOperativo.builder()
                        .moduloServicio("CONSULTAS")
                        .tipoEvento("SESION_CPaaS_INICIADA")
                        .descripcion("Videollamada iniciada correctamente con proveedor CPaaS HIPAA.")
                        .latenciaMs(32L)
                        .severidad(Severidad.INFO)
                        .build());

                repository.save(ReporteOperativo.builder()
                        .moduloServicio("FICHAS")
                        .tipoEvento("SINCRONIZACION_OK")
                        .descripcion("Sincronización de atención remota con software de ficha clínica rural exitosa.")
                        .latenciaMs(55L)
                        .severidad(Severidad.INFO)
                        .build());
            }
        };
    }
}
