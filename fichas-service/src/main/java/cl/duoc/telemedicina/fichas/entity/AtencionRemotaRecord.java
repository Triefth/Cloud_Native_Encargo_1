package cl.duoc.telemedicina.fichas.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "atenciones_remotas_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
