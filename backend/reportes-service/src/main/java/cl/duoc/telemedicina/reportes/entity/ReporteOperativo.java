package cl.duoc.telemedicina.reportes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_operativos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @PrePersist
    public void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.severidad == null) {
            this.severidad = Severidad.INFO;
        }
    }

    public enum Severidad {
        INFO,
        WARNING,
        CRITICAL
    }
}
