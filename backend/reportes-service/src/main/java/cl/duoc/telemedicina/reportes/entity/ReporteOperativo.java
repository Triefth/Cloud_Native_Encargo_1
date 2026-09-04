package cl.duoc.telemedicina.reportes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_operativos")
public class ReporteOperativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El módulo de servicio es obligatorio")
    private String moduloServicio;

    @NotBlank(message = "El tipo de evento es obligatorio")
    private String tipoEvento;

    @Column(length = 2000)
    private String descripcion;

    private Long latenciaMs;

    @Enumerated(EnumType.STRING)
    private Severidad severidad;

    private LocalDateTime fechaRegistro;

    public ReporteOperativo() {
    }

    public ReporteOperativo(Long id, String moduloServicio, String tipoEvento, String descripcion, Long latenciaMs, Severidad severidad, LocalDateTime fechaRegistro) {
        this.id = id;
        this.moduloServicio = moduloServicio;
        this.tipoEvento = tipoEvento;
        this.descripcion = descripcion;
        this.latenciaMs = latenciaMs;
        this.severidad = severidad;
        this.fechaRegistro = fechaRegistro;
    }

    @PrePersist
    public void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.severidad == null) {
            this.severidad = Severidad.INFO;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModuloServicio() { return moduloServicio; }
    public void setModuloServicio(String moduloServicio) { this.moduloServicio = moduloServicio; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getLatenciaMs() { return latenciaMs; }
    public void setLatenciaMs(Long latenciaMs) { this.latenciaMs = latenciaMs; }

    public Severidad getSeveridad() { return severidad; }
    public void setSeveridad(Severidad severidad) { this.severidad = severidad; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String moduloServicio;
        private String tipoEvento;
        private String descripcion;
        private Long latenciaMs;
        private Severidad severidad;
        private LocalDateTime fechaRegistro;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder moduloServicio(String moduloServicio) { this.moduloServicio = moduloServicio; return this; }
        public Builder tipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; return this; }
        public Builder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public Builder latenciaMs(Long latenciaMs) { this.latenciaMs = latenciaMs; return this; }
        public Builder severidad(Severidad severidad) { this.severidad = severidad; return this; }
        public Builder fechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; return this; }

        public ReporteOperativo build() {
            return new ReporteOperativo(id, moduloServicio, tipoEvento, descripcion, latenciaMs, severidad, fechaRegistro);
        }
    }

    public enum Severidad {
        INFO,
        WARNING,
        CRITICAL
    }
}
