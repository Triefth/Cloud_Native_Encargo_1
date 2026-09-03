package cl.duoc.telemedicina.clinicas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "clinicas_rurales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicaRural {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la clínica es obligatorio")
    @Column(unique = true)
    private String nombre;

    @NotBlank(message = "La comuna/región es obligatoria")
    private String comuna;

    private String region;

    private String direccion;

    private String telefono;

    private String emailContacto;

    private Boolean enMarchaBlanca;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "configuracion_ehr_id")
    private ConfiguracionEhr configuracionEhr;
}
