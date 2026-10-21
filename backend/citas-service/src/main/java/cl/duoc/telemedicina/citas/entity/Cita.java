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

    // Getters y Setters explícitos para soporte universal en cualquier IDE
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRutPaciente() { return rutPaciente; }
    public void setRutPaciente(String rutPaciente) { this.rutPaciente = rutPaciente; }

    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }

    public String getRutMedico() { return rutMedico; }
    public void setRutMedico(String rutMedico) { this.rutMedico = rutMedico; }

    public String getNombreMedico() { return nombreMedico; }
    public void setNombreMedico(String nombreMedico) { this.nombreMedico = nombreMedico; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }

    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }

    public String getClinicaRural() { return clinicaRural; }
    public void setClinicaRural(String clinicaRural) { this.clinicaRural = clinicaRural; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
