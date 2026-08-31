package cl.duoc.telemedicina.fichas.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fichas_medicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RUT del paciente es obligatorio")
    @Column(unique = true)
    private String rutPaciente;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    private String fechaNacimiento;

    private String grupoSanguineo;

    private String alergias;

    private String enfermedadesCronicas;

    private String clinicaOrigen;

    @OneToMany(mappedBy = "fichaMedica", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default
    private List<AtencionRemotaRecord> atencionesRemotas = new ArrayList<>();
}
