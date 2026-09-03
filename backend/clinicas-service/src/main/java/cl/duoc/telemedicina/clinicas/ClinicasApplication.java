package cl.duoc.telemedicina.clinicas;

import cl.duoc.telemedicina.clinicas.entity.ClinicaRural;
import cl.duoc.telemedicina.clinicas.entity.ConfiguracionEhr;
import cl.duoc.telemedicina.clinicas.repository.ClinicaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClinicasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicasApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(ClinicaRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                ConfiguracionEhr configSanPedro = ConfiguracionEhr.builder()
                        .nombreSoftwareEhr("Rayen Salud EHR")
                        .apiEndpointUrl("https://api.rayen.sanpedro.cl/v2/fichas")
                        .tipoProtocolo("REST_JSON")
                        .apiKey("SP-KEY-RURAL-84920")
                        .timeoutMs("5000")
                        .sincronizacionAutomatica(true)
                        .build();

                repository.save(ClinicaRural.builder()
                        .nombre("Clínica Rural San Pedro de Atacama")
                        .comuna("San Pedro de Atacama")
                        .region("Antofagasta")
                        .direccion("Calle Licancabur 450")
                        .telefono("+56552851000")
                        .emailContacto("contacto@clinicasanpedro.cl")
                        .enMarchaBlanca(true)
                        .configuracionEhr(configSanPedro)
                        .build());

                ConfiguracionEhr configHuara = ConfiguracionEhr.builder()
                        .nombreSoftwareEhr("HL7 FHIR Adapter Huara")
                        .apiEndpointUrl("https://fhir.clinicahuara.cl/r4")
                        .tipoProtocolo("HL7_FHIR")
                        .apiKey("HUARA-FHIR-SEC-992")
                        .timeoutMs("3000")
                        .sincronizacionAutomatica(true)
                        .build();

                repository.save(ClinicaRural.builder()
                        .nombre("Clínica Rural de Huara")
                        .comuna("Huara")
                        .region("Tarapacá")
                        .direccion("Av. Prat 120")
                        .telefono("+56572481234")
                        .emailContacto("salud@huara.cl")
                        .enMarchaBlanca(false)
                        .configuracionEhr(configHuara)
                        .build());
            }
        };
    }
}
