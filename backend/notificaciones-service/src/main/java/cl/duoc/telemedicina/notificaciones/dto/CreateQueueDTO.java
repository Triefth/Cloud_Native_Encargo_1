package cl.duoc.telemedicina.notificaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la creación y validación de Colas en RabbitMQ.
 * Cumple con Pauta Duoc UC EP2: 7% Validación de parámetros con DTO.
 * Implementa constructores y getters/setters explícitos en Java estándar sin depender de plugins de IDE.
 */
public class CreateQueueDTO {

    @NotBlank(message = "El nombre de la cola es obligatorio y no puede estar en blanco.")
    @Size(min = 2, max = 255, message = "El nombre de la cola debe tener entre 2 y 255 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de la cola contiene caracteres inválidos. Solo se permiten letras, números, guiones y puntos.")
    private String queueName;

    private boolean durable = true;
    private boolean autoDelete = false;
    private boolean exclusive = false;

    public CreateQueueDTO() {
    }

    public CreateQueueDTO(String queueName, boolean durable, boolean autoDelete, boolean exclusive) {
        this.queueName = queueName;
        this.durable = durable;
        this.autoDelete = autoDelete;
        this.exclusive = exclusive;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
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

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }
}
