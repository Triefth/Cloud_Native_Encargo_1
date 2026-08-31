package cl.duoc.telemedicina.consultas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long citaId;

    @NotBlank(message = "El RUT del paciente es obligatorio")
    private String rutPaciente;

    @NotBlank(message = "El RUT del médico es obligatorio")
    private String rutMedico;

    private String roomName;

    private String cpaasJoinUrl;

    @Enumerated(EnumType.STRING)
    private EstadoConsulta estado;

    @Column(length = 2000)
    private String diagnosticoPreliminar;

    @Column(length = 2000)
    private String indicacionesMedicas;

    private Integer duracionMinutos;

    private LocalDateTime fechaInicio;
    
    private LocalDateTime fechaFin;

    public enum EstadoConsulta {
        PENDIENTE,
        EN_CURSO,
        FINALIZADA
    }
}
