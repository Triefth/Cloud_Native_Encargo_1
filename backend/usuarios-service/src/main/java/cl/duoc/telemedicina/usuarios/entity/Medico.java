package cl.duoc.telemedicina.usuarios.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "medicos")
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

    public Medico() {
    }

    public Medico(Long id, String rut, String nombreCompleto, String especialidad, String registroSuperintendencia, String email, String telefono, Boolean esVoluntarioActivo, String ciudadBase) {
        this.id = id;
        this.rut = rut;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.registroSuperintendencia = registroSuperintendencia;
        this.email = email;
        this.telefono = telefono;
        this.esVoluntarioActivo = esVoluntarioActivo;
        this.ciudadBase = ciudadBase;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getRegistroSuperintendencia() { return registroSuperintendencia; }
    public void setRegistroSuperintendencia(String registroSuperintendencia) { this.registroSuperintendencia = registroSuperintendencia; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Boolean getEsVoluntarioActivo() { return esVoluntarioActivo; }
    public void setEsVoluntarioActivo(Boolean esVoluntarioActivo) { this.esVoluntarioActivo = esVoluntarioActivo; }

    public String getCiudadBase() { return ciudadBase; }
    public void setCiudadBase(String ciudadBase) { this.ciudadBase = ciudadBase; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String rut;
        private String nombreCompleto;
        private String especialidad;
        private String registroSuperintendencia;
        private String email;
        private String telefono;
        private Boolean esVoluntarioActivo;
        private String ciudadBase;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder rut(String rut) { this.rut = rut; return this; }
        public Builder nombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; return this; }
        public Builder especialidad(String especialidad) { this.especialidad = especialidad; return this; }
        public Builder registroSuperintendencia(String registroSuperintendencia) { this.registroSuperintendencia = registroSuperintendencia; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder telefono(String telefono) { this.telefono = telefono; return this; }
        public Builder esVoluntarioActivo(Boolean esVoluntarioActivo) { this.esVoluntarioActivo = esVoluntarioActivo; return this; }
        public Builder ciudadBase(String ciudadBase) { this.ciudadBase = ciudadBase; return this; }

        public Medico build() {
            return new Medico(id, rut, nombreCompleto, especialidad, registroSuperintendencia, email, telefono, esVoluntarioActivo, ciudadBase);
        }
    }
}
