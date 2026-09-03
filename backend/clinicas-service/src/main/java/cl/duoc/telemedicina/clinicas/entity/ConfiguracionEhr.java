package cl.duoc.telemedicina.clinicas.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuraciones_ehr")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionEhr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreSoftwareEhr; // ej. Rayen, Medisyn, HL7 FHIR Custom API

    private String apiEndpointUrl;

    private String tipoProtocolo; // REST_JSON, HL7_FHIR, SOAP

    private String apiKey;

    private String timeoutMs;

    private Boolean sincronizacionAutomatica;
}
