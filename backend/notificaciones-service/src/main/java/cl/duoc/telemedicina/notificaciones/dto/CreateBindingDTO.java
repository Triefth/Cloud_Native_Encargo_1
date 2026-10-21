package cl.duoc.telemedicina.notificaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la creación y validación de Bindings en RabbitMQ.
 * Cumple con Pauta Duoc UC EP2: 7% Validación de parámetros con DTO.
 * Implementa constructores y getters/setters explícitos en Java estándar sin depender de plugins de IDE.
 */
public class CreateBindingDTO {

    @NotBlank(message = "El nombre de la cola es obligatorio.")
    @Size(min = 2, max = 255, message = "El nombre de la cola debe tener entre 2 y 255 caracteres.")
    private String queueName;

    @NotBlank(message = "El nombre del exchange es obligatorio.")
    @Size(min = 2, max = 255, message = "El nombre del exchange debe tener entre 2 y 255 caracteres.")
    private String exchangeName;

    @NotBlank(message = "La routing key es obligatoria.")
    private String routingKey;

    public CreateBindingDTO() {
    }

    public CreateBindingDTO(String queueName, String exchangeName, String routingKey) {
        this.queueName = queueName;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
}
