package cl.duoc.telemedicina.clinicas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "clinicas_rurales")
public class ClinicaRural {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la clínica es obligatorio")
    @Column(unique = true)
    private String nombre;

    @NotBlank(message = "La comuna/región es obligatoria")
    private String comuna;

    private String region;

    private String direccion;

    private String telefono;

    private String emailContacto;

    private Boolean enMarchaBlanca;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "configuracion_ehr_id")
    private ConfiguracionEhr configuracionEhr;

    public ClinicaRural() {
    }

    public ClinicaRural(Long id, String nombre, String comuna, String region, String direccion, String telefono, String emailContacto, Boolean enMarchaBlanca, ConfiguracionEhr configuracionEhr) {
        this.id = id;
        this.nombre = nombre;
        this.comuna = comuna;
        this.region = region;
        this.direccion = direccion;
        this.telefono = telefono;
        this.emailContacto = emailContacto;
        this.enMarchaBlanca = enMarchaBlanca;
        this.configuracionEhr = configuracionEhr;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }

    public Boolean getEnMarchaBlanca() { return enMarchaBlanca; }
    public void setEnMarchaBlanca(Boolean enMarchaBlanca) { this.enMarchaBlanca = enMarchaBlanca; }

    public ConfiguracionEhr getConfiguracionEhr() { return configuracionEhr; }
    public void setConfiguracionEhr(ConfiguracionEhr configuracionEhr) { this.configuracionEhr = configuracionEhr; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String nombre;
        private String comuna;
        private String region;
        private String direccion;
        private String telefono;
        private String emailContacto;
        private Boolean enMarchaBlanca;
        private ConfiguracionEhr configuracionEhr;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder nombre(String nombre) { this.nombre = nombre; return this; }
        public Builder comuna(String comuna) { this.comuna = comuna; return this; }
        public Builder region(String region) { this.region = region; return this; }
        public Builder direccion(String direccion) { this.direccion = direccion; return this; }
        public Builder telefono(String telefono) { this.telefono = telefono; return this; }
        public Builder emailContacto(String emailContacto) { this.emailContacto = emailContacto; return this; }
        public Builder enMarchaBlanca(Boolean enMarchaBlanca) { this.enMarchaBlanca = enMarchaBlanca; return this; }
        public Builder configuracionEhr(ConfiguracionEhr configuracionEhr) { this.configuracionEhr = configuracionEhr; return this; }

        public ClinicaRural build() {
            return new ClinicaRural(id, nombre, comuna, region, direccion, telefono, emailContacto, enMarchaBlanca, configuracionEhr);
        }
    }
}
