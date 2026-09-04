package cl.duoc.telemedicina.consultas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultas")
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

    public Consulta() {
    }

    public Consulta(Long id, Long citaId, String rutPaciente, String rutMedico, String roomName, String cpaasJoinUrl, EstadoConsulta estado, String diagnosticoPreliminar, String indicacionesMedicas, Integer duracionMinutos, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.id = id;
        this.citaId = citaId;
        this.rutPaciente = rutPaciente;
        this.rutMedico = rutMedico;
        this.roomName = roomName;
        this.cpaasJoinUrl = cpaasJoinUrl;
        this.estado = estado;
        this.diagnosticoPreliminar = diagnosticoPreliminar;
        this.indicacionesMedicas = indicacionesMedicas;
        this.duracionMinutos = duracionMinutos;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public String getRutPaciente() {
        return rutPaciente;
    }

    public void setRutPaciente(String rutPaciente) {
        this.rutPaciente = rutPaciente;
    }

    public String getRutMedico() {
        return rutMedico;
    }

    public void setRutMedico(String rutMedico) {
        this.rutMedico = rutMedico;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getCpaasJoinUrl() {
        return cpaasJoinUrl;
    }

    public void setCpaasJoinUrl(String cpaasJoinUrl) {
        this.cpaasJoinUrl = cpaasJoinUrl;
    }

    public EstadoConsulta getEstado() {
        return estado;
    }

    public void setEstado(EstadoConsulta estado) {
        this.estado = estado;
    }

    public String getDiagnosticoPreliminar() {
        return diagnosticoPreliminar;
    }

    public void setDiagnosticoPreliminar(String diagnosticoPreliminar) {
        this.diagnosticoPreliminar = diagnosticoPreliminar;
    }

    public String getIndicacionesMedicas() {
        return indicacionesMedicas;
    }

    public void setIndicacionesMedicas(String indicacionesMedicas) {
        this.indicacionesMedicas = indicacionesMedicas;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long citaId;
        private String rutPaciente;
        private String rutMedico;
        private String roomName;
        private String cpaasJoinUrl;
        private EstadoConsulta estado;
        private String diagnosticoPreliminar;
        private String indicacionesMedicas;
        private Integer duracionMinutos;
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaFin;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder citaId(Long citaId) { this.citaId = citaId; return this; }
        public Builder rutPaciente(String rutPaciente) { this.rutPaciente = rutPaciente; return this; }
        public Builder rutMedico(String rutMedico) { this.rutMedico = rutMedico; return this; }
        public Builder roomName(String roomName) { this.roomName = roomName; return this; }
        public Builder cpaasJoinUrl(String cpaasJoinUrl) { this.cpaasJoinUrl = cpaasJoinUrl; return this; }
        public Builder estado(EstadoConsulta estado) { this.estado = estado; return this; }
        public Builder diagnosticoPreliminar(String diagnosticoPreliminar) { this.diagnosticoPreliminar = diagnosticoPreliminar; return this; }
        public Builder indicacionesMedicas(String indicacionesMedicas) { this.indicacionesMedicas = indicacionesMedicas; return this; }
        public Builder duracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; return this; }
        public Builder fechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; return this; }
        public Builder fechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; return this; }

        public Consulta build() {
            return new Consulta(id, citaId, rutPaciente, rutMedico, roomName, cpaasJoinUrl, estado, diagnosticoPreliminar, indicacionesMedicas, duracionMinutos, fechaInicio, fechaFin);
        }
    }

    public enum EstadoConsulta {
        PENDIENTE,
        EN_CURSO,
        FINALIZADA
    }
}
