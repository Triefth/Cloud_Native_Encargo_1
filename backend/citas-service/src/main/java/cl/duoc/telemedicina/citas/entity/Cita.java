package cl.duoc.telemedicina.citas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
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

    public Cita() {
    }

    public Cita(Long id, String rutPaciente, String nombrePaciente, String rutMedico, String nombreMedico, String especialidad, LocalDateTime fechaHora, EstadoCita estado, String motivoConsulta, String clinicaRural, LocalDateTime fechaCreacion) {
        this.id = id;
        this.rutPaciente = rutPaciente;
        this.nombrePaciente = nombrePaciente;
        this.rutMedico = rutMedico;
        this.nombreMedico = nombreMedico;
        this.especialidad = especialidad;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.motivoConsulta = motivoConsulta;
        this.clinicaRural = clinicaRural;
        this.fechaCreacion = fechaCreacion;
    }

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String rutPaciente;
        private String nombrePaciente;
        private String rutMedico;
        private String nombreMedico;
        private String especialidad;
        private LocalDateTime fechaHora;
        private EstadoCita estado;
        private String motivoConsulta;
        private String clinicaRural;
        private LocalDateTime fechaCreacion;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder rutPaciente(String rutPaciente) { this.rutPaciente = rutPaciente; return this; }
        public Builder nombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; return this; }
        public Builder rutMedico(String rutMedico) { this.rutMedico = rutMedico; return this; }
        public Builder nombreMedico(String nombreMedico) { this.nombreMedico = nombreMedico; return this; }
        public Builder especialidad(String especialidad) { this.especialidad = especialidad; return this; }
        public Builder fechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; return this; }
        public Builder estado(EstadoCita estado) { this.estado = estado; return this; }
        public Builder motivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; return this; }
        public Builder clinicaRural(String clinicaRural) { this.clinicaRural = clinicaRural; return this; }
        public Builder fechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; return this; }

        public Cita build() {
            return new Cita(id, rutPaciente, nombrePaciente, rutMedico, nombreMedico, especialidad, fechaHora, estado, motivoConsulta, clinicaRural, fechaCreacion);
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
