package cl.duoc.telemedicina.usuarios.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "medicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RUT del médico es obligatorio")
    @Column(unique = true)
    private String rut;

    @NotBlank(message = "El nombre del médico es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "La especialidad es obligatoria")
    private String especialidad;

    private String registroSuperintendencia;

    private String email;

    private String telefono;

    private Boolean esVoluntarioActivo;

    private String ciudadBase;
}
