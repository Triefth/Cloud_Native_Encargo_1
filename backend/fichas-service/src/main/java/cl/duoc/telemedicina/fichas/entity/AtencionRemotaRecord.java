package cl.duoc.telemedicina.fichas.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "atenciones_remotas_records")
public class AtencionRemotaRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ficha_id")
    @JsonBackReference
    private FichaMedica fichaMedica;

    private Long consultaId;

    private String nombreMedico;

    private String especialidad;

    @Column(length = 2000)
    private String resumenAtencion;

    private LocalDateTime fechaAtencion;

    private String origenSistema;

    public AtencionRemotaRecord() {
    }

    public AtencionRemotaRecord(Long id, FichaMedica fichaMedica, Long consultaId, String nombreMedico, String especialidad, String resumenAtencion, LocalDateTime fechaAtencion, String origenSistema) {
        this.id = id;
        this.fichaMedica = fichaMedica;
        this.consultaId = consultaId;
        this.nombreMedico = nombreMedico;
        this.especialidad = especialidad;
        this.resumenAtencion = resumenAtencion;
        this.fechaAtencion = fechaAtencion;
        this.origenSistema = origenSistema;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FichaMedica getFichaMedica() { return fichaMedica; }
    public void setFichaMedica(FichaMedica fichaMedica) { this.fichaMedica = fichaMedica; }

    public Long getConsultaId() { return consultaId; }
    public void setConsultaId(Long consultaId) { this.consultaId = consultaId; }

    public String getNombreMedico() { return nombreMedico; }
    public void setNombreMedico(String nombreMedico) { this.nombreMedico = nombreMedico; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getResumenAtencion() { return resumenAtencion; }
    public void setResumenAtencion(String resumenAtencion) { this.resumenAtencion = resumenAtencion; }

    public LocalDateTime getFechaAtencion() { return fechaAtencion; }
    public void setFechaAtencion(LocalDateTime fechaAtencion) { this.fechaAtencion = fechaAtencion; }

    public String getOrigenSistema() { return origenSistema; }
    public void setOrigenSistema(String origenSistema) { this.origenSistema = origenSistema; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private FichaMedica fichaMedica;
        private Long consultaId;
        private String nombreMedico;
        private String especialidad;
        private String resumenAtencion;
        private LocalDateTime fechaAtencion;
        private String origenSistema;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder fichaMedica(FichaMedica fichaMedica) { this.fichaMedica = fichaMedica; return this; }
        public Builder consultaId(Long consultaId) { this.consultaId = consultaId; return this; }
        public Builder nombreMedico(String nombreMedico) { this.nombreMedico = nombreMedico; return this; }
        public Builder especialidad(String especialidad) { this.especialidad = especialidad; return this; }
        public Builder resumenAtencion(String resumenAtencion) { this.resumenAtencion = resumenAtencion; return this; }
        public Builder fechaAtencion(LocalDateTime fechaAtencion) { this.fechaAtencion = fechaAtencion; return this; }
        public Builder origenSistema(String origenSistema) { this.origenSistema = origenSistema; return this; }

        public AtencionRemotaRecord build() {
            return new AtencionRemotaRecord(id, fichaMedica, consultaId, nombreMedico, especialidad, resumenAtencion, fechaAtencion, origenSistema);
        }
    }
}
