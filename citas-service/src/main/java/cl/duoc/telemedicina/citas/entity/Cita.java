package cl.duoc.telemedicina.citas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RUT del paciente es obligatorio")
    private String rutPaciente;

    @NotBlank(message = "El nombre del paciente es obligatorio")
    private String nombrePaciente;

    @NotBlank(message = "El RUT del médico es obligatorio")
    private String rutMedico;

    @NotBlank(message = "El nombre del médico es obligatorio")
    private String nombreMedico;

    private String especialidad;

    @NotNull(message = "La fecha y hora de la cita es obligatoria")
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    private String motivoConsulta;

    private String clinicaRural;

    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoCita.PROGRAMADA;
        }
        if (this.clinicaRural == null) {
            this.clinicaRural = "Clínica Rural San Pedro";
        }
    }

    public enum EstadoCita {
        PROGRAMADA,
        CONFIRMADA,
        CANCELADA,
        REPROGRAMADA,
        FINALIZADA
    }
}