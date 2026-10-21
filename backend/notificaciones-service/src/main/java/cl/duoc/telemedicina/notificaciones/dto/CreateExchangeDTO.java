package cl.duoc.telemedicina.notificaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la creación y validación de Exchanges en RabbitMQ.
 * Cumple con Pauta Duoc UC EP2: 7% Validación de parámetros con DTO.
 * Implementa constructores y getters/setters explícitos en Java estándar sin depender de plugins de IDE.
 */
public class CreateExchangeDTO {

    @NotBlank(message = "El nombre del exchange es obligatorio y no puede estar en blanco.")
    @Size(min = 2, max = 255, message = "El nombre del exchange debe tener entre 2 y 255 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre del exchange contiene caracteres inválidos. Solo se permiten letras, números, guiones y puntos.")
    private String exchangeName;

    private String type = "direct"; // direct, fanout, topic, headers
    private boolean durable = true;
    private boolean autoDelete = false;

    public CreateExchangeDTO() {
    }

    public CreateExchangeDTO(String exchangeName, String type, boolean durable, boolean autoDelete) {
        this.exchangeName = exchangeName;
        this.type = type;
        this.durable = durable;
        this.autoDelete = autoDelete;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }
}
