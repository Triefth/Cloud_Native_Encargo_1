package cl.duoc.telemedicina.notificaciones.service;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitResourceManager {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    public void createQueue(String queueName) {
        try {
            Queue queue = QueueBuilder.durable(queueName).build();
            rabbitAdmin.declareQueue(queue);
            System.out.println("[✓] Cola creada: " + queueName);
        } catch (Exception e) {
            System.err.println("[✗] Error al crear cola " + queueName + ": " + e.getMessage());
        }
    }

    public void createExchange(String exchangeName) {
        try {
            Exchange exchange = new DirectExchange(exchangeName, true, false);
            rabbitAdmin.declareExchange(exchange);
            System.out.println("[✓] Exchange creado: " + exchangeName);
        } catch (Exception e) {
            System.err.println("[✗] Error al crear exchange " + exchangeName + ": " + e.getMessage());
        }
    }

    public void createBinding(String queueName, String exchangeName, String routingKey) {
        try {
            Binding binding = BindingBuilder
                    .bind(new Queue(queueName))
                    .to(new DirectExchange(exchangeName))
                    .with(routingKey);
            rabbitAdmin.declareBinding(binding);
            System.out.println("[✓] Binding creado: " + queueName + " <-> " + exchangeName);
        } catch (Exception e) {
            System.err.println("[✗] Error al crear binding: " + e.getMessage());
        }
    }

    public QueueInformation getQueueInfo(String queueName) {
        try {
            return rabbitAdmin.getQueueInfo(queueName);
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteQueue(String queueName) {
        try {
            rabbitAdmin.deleteQueue(queueName);
            System.out.println("[✓] Cola eliminada: " + queueName);
        } catch (Exception e) {
            System.err.println("[✗] Error al eliminar cola: " + e.getMessage());
        }
    }

    public void deleteExchange(String exchangeName) {
        try {
            rabbitAdmin.deleteExchange(exchangeName);
            System.out.println("[✓] Exchange eliminado: " + exchangeName);
        } catch (Exception e) {
            System.err.println("[✗] Error al eliminar exchange: " + e.getMessage());
        }
    }

    public void deleteBinding(String queueName, String exchangeName, String routingKey) {
        try {
            Binding binding = BindingBuilder
                    .bind(new Queue(queueName))
                    .to(new DirectExchange(exchangeName))
                    .with(routingKey);
            rabbitAdmin.removeBinding(binding);
            System.out.println("[✓] Binding eliminado: " + queueName + " <-> " + exchangeName);
        } catch (Exception e) {
            System.err.println("[✗] Error al eliminar binding: " + e.getMessage());
        }
    }

    public void purgeQueue(String queueName) {
        try {
            int purged = rabbitAdmin.purgeQueue(queueName);
            System.out.println("[✓] Cola purgada: " + queueName + ", mensajes eliminados: " + purged);
        } catch (Exception e) {
            System.err.println("[✗] Error al purgar cola: " + e.getMessage());
        }
    }
}
