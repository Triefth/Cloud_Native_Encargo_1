package cl.duoc.telemedicina.notificaciones.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long citaId;

    @NotBlank(message = "El RUT del paciente es obligatorio")
    private String rutPaciente;

    @Enumerated(EnumType.STRING)
    private TipoNotificacion tipo;

    @Column(length = 1000)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    private EstadoEnvio estado;

    private LocalDateTime fechaEnvio;

    private Boolean confirmacionLectura;

    public Notificacion() {
    }

    public Notificacion(Long id, Long citaId, String rutPaciente, TipoNotificacion tipo, String mensaje, EstadoEnvio estado, LocalDateTime fechaEnvio, Boolean confirmacionLectura) {
        this.id = id;
        this.citaId = citaId;
        this.rutPaciente = rutPaciente;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.estado = estado;
        this.fechaEnvio = fechaEnvio;
        this.confirmacionLectura = confirmacionLectura;
    }

    @PrePersist
    public void prePersist() {
        this.fechaEnvio = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoEnvio.ENVIADO;
        }
        if (this.confirmacionLectura == null) {
            this.confirmacionLectura = false;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCitaId() { return citaId; }
    public void setCitaId(Long citaId) { this.citaId = citaId; }

    public String getRutPaciente() { return rutPaciente; }
    public void setRutPaciente(String rutPaciente) { this.rutPaciente = rutPaciente; }

    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public EstadoEnvio getEstado() { return estado; }
    public void setEstado(EstadoEnvio estado) { this.estado = estado; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public Boolean getConfirmacionLectura() { return confirmacionLectura; }
    public void setConfirmacionLectura(Boolean confirmacionLectura) { this.confirmacionLectura = confirmacionLectura; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long citaId;
        private String rutPaciente;
        private TipoNotificacion tipo;
        private String mensaje;
        private EstadoEnvio estado;
        private LocalDateTime fechaEnvio;
        private Boolean confirmacionLectura;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder citaId(Long citaId) { this.citaId = citaId; return this; }
        public Builder rutPaciente(String rutPaciente) { this.rutPaciente = rutPaciente; return this; }
        public Builder tipo(TipoNotificacion tipo) { this.tipo = tipo; return this; }
        public Builder mensaje(String mensaje) { this.mensaje = mensaje; return this; }
        public Builder estado(EstadoEnvio estado) { this.estado = estado; return this; }
        public Builder fechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; return this; }
        public Builder confirmacionLectura(Boolean confirmacionLectura) { this.confirmacionLectura = confirmacionLectura; return this; }

        public Notificacion build() {
            return new Notificacion(id, citaId, rutPaciente, tipo, mensaje, estado, fechaEnvio, confirmacionLectura);
        }
    }

    public enum TipoNotificacion {
        SMS,
        WHATSAPP,
        EMAIL
    }

    public enum EstadoEnvio {
        PENDIENTE,
        ENVIADO,
        FALLIDO
    }
}
