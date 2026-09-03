package cl.duoc.telemedicina.notificaciones.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
