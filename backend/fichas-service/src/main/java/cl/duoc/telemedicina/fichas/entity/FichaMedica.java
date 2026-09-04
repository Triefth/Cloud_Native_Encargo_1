package cl.duoc.telemedicina.fichas.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fichas_medicas")
public class FichaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RUT del paciente es obligatorio")
    @Column(unique = true)
    private String rutPaciente;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    private String fechaNacimiento;

    private String grupoSanguineo;

    private String alergias;

    private String enfermedadesCronicas;

    private String clinicaOrigen;

    @OneToMany(mappedBy = "fichaMedica", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<AtencionRemotaRecord> atencionesRemotas = new ArrayList<>();

    public FichaMedica() {
    }

    public FichaMedica(Long id, String rutPaciente, String nombreCompleto, String fechaNacimiento, String grupoSanguineo, String alergias, String enfermedadesCronicas, String clinicaOrigen, List<AtencionRemotaRecord> atencionesRemotas) {
        this.id = id;
        this.rutPaciente = rutPaciente;
        this.nombreCompleto = nombreCompleto;
        this.fechaNacimiento = fechaNacimiento;
        this.grupoSanguineo = grupoSanguineo;
        this.alergias = alergias;
        this.enfermedadesCronicas = enfermedadesCronicas;
        this.clinicaOrigen = clinicaOrigen;
        if (atencionesRemotas != null) {
            this.atencionesRemotas = atencionesRemotas;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRutPaciente() { return rutPaciente; }
    public void setRutPaciente(String rutPaciente) { this.rutPaciente = rutPaciente; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getGrupoSanguineo() { return grupoSanguineo; }
    public void setGrupoSanguineo(String grupoSanguineo) { this.grupoSanguineo = grupoSanguineo; }

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public String getEnfermedadesCronicas() { return enfermedadesCronicas; }
    public void setEnfermedadesCronicas(String enfermedadesCronicas) { this.enfermedadesCronicas = enfermedadesCronicas; }

    public String getClinicaOrigen() { return clinicaOrigen; }
    public void setClinicaOrigen(String clinicaOrigen) { this.clinicaOrigen = clinicaOrigen; }

    public List<AtencionRemotaRecord> getAtencionesRemotas() { return atencionesRemotas; }
    public void setAtencionesRemotas(List<AtencionRemotaRecord> atencionesRemotas) { this.atencionesRemotas = atencionesRemotas; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String rutPaciente;
        private String nombreCompleto;
        private String fechaNacimiento;
        private String grupoSanguineo;
        private String alergias;
        private String enfermedadesCronicas;
        private String clinicaOrigen;
        private List<AtencionRemotaRecord> atencionesRemotas = new ArrayList<>();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder rutPaciente(String rutPaciente) { this.rutPaciente = rutPaciente; return this; }
        public Builder nombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; return this; }
        public Builder fechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; return this; }
        public Builder grupoSanguineo(String grupoSanguineo) { this.grupoSanguineo = grupoSanguineo; return this; }
        public Builder alergias(String alergias) { this.alergias = alergias; return this; }
        public Builder enfermedadesCronicas(String enfermedadesCronicas) { this.enfermedadesCronicas = enfermedadesCronicas; return this; }
        public Builder clinicaOrigen(String clinicaOrigen) { this.clinicaOrigen = clinicaOrigen; return this; }
        public Builder atencionesRemotas(List<AtencionRemotaRecord> atencionesRemotas) { this.atencionesRemotas = atencionesRemotas; return this; }

        public FichaMedica build() {
            return new FichaMedica(id, rutPaciente, nombreCompleto, fechaNacimiento, grupoSanguineo, alergias, enfermedadesCronicas, clinicaOrigen, atencionesRemotas);
        }
    }
}
