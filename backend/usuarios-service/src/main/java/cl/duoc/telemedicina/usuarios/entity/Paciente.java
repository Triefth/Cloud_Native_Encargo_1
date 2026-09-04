package cl.duoc.telemedicina.usuarios.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RUT es obligatorio")
    @Column(unique = true)
    private String rut;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombres;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apellidoPaterno;

    private String apellidoMaterno;

    private LocalDate fechaNacimiento;

    private String telefonoContacto;

    private String email;

    private String direccionRural;

    private String clinicaAsignada;

    public Paciente() {
    }

    public Paciente(Long id, String rut, String nombres, String apellidoPaterno, String apellidoMaterno, LocalDate fechaNacimiento, String telefonoContacto, String email, String direccionRural, String clinicaAsignada) {
        this.id = id;
        this.rut = rut;
        this.nombres = nombres;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.fechaNacimiento = fechaNacimiento;
        this.telefonoContacto = telefonoContacto;
        this.email = email;
        this.direccionRural = direccionRural;
        this.clinicaAsignada = clinicaAsignada;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccionRural() { return direccionRural; }
    public void setDireccionRural(String direccionRural) { this.direccionRural = direccionRural; }

    public String getClinicaAsignada() { return clinicaAsignada; }
    public void setClinicaAsignada(String clinicaAsignada) { this.clinicaAsignada = clinicaAsignada; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String rut;
        private String nombres;
        private String apellidoPaterno;
        private String apellidoMaterno;
        private LocalDate fechaNacimiento;
        private String telefonoContacto;
        private String email;
        private String direccionRural;
        private String clinicaAsignada;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder rut(String rut) { this.rut = rut; return this; }
        public Builder nombres(String nombres) { this.nombres = nombres; return this; }
        public Builder apellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; return this; }
        public Builder apellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; return this; }
        public Builder fechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; return this; }
        public Builder telefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder direccionRural(String direccionRural) { this.direccionRural = direccionRural; return this; }
        public Builder clinicaAsignada(String clinicaAsignada) { this.clinicaAsignada = clinicaAsignada; return this; }

        public Paciente build() {
            return new Paciente(id, rut, nombres, apellidoPaterno, apellidoMaterno, fechaNacimiento, telefonoContacto, email, direccionRural, clinicaAsignada);
        }
    }
}
