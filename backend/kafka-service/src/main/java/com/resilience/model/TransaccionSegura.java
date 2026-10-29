package com.resilience.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Modelo de Transacción con Validación de Payloads (JSR-380 / Bean Validation)
 * e Identificador Único para Idempotencia del Consumidor.
 * Según PDF 1 (Páginas 6 y 9 de Duoc UC).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransaccionSegura implements Serializable {

    @NotBlank(message = "El correlationId / messageId es obligatorio para garantizar idempotencia y trazabilidad")
    private String messageId;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String usuario;

    @NotNull(message = "El monto no puede ser nulo")
    @Min(value = 1, message = "El monto debe ser mayor o igual a $1.0")
    private Double monto;

    @NotBlank(message = "El origen es obligatorio (ej. app, web, cajero)")
    private String origen;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe tener un formato válido (ej. usuario@banco.cl)")
    private String email;

    private Long timestamp;

    /**
     * Bandera para simular un fallo transitorio o permanente en pruebas educativas
     * "transient_error", "permanent_error", "none"
     */
    private String simulationErrorMode;
}
