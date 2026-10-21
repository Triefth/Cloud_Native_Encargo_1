package cl.duoc.telemedicina.notificaciones.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * =====================================================================
 * RabbitResourceManager: Servicio de Gestión Programática de Recursos
 * Cumple con Pauta Duoc UC EP2:
 *  - 10% Lógica de creación/eliminación encapsulada en un servicio dedicado
 *  - 13% Endpoints REST consumen este servicio sin conocer detalles internos
 * =====================================================================
 */
@Service
public class RabbitResourceManager {

    private static final Logger log = LoggerFactory.getLogger(RabbitResourceManager.class);

    @Autowired
    private RabbitAdmin rabbitAdmin;

    public RabbitResourceManager() {
    }

    public RabbitResourceManager(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public void createQueue(String queueName, boolean durable) {
        try {
            Queue queue = QueueBuilder.durable(queueName).build();
            if (!durable) {
                queue = QueueBuilder.nonDurable(queueName).build();
            }
            rabbitAdmin.declareQueue(queue);
            log.info("[✓] Cola creada exitosamente: {}", queueName);
        } catch (Exception e) {
            log.error("[✗] Error al crear cola {}: {}", queueName, e.getMessage());
            throw new RuntimeException("Error al declarar cola en RabbitMQ: " + e.getMessage(), e);
        }
    }

    public void createQueue(String queueName) {
        createQueue(queueName, true);
    }

    public void createExchange(String exchangeName, String type, boolean durable) {
        try {
            Exchange exchange;
            if ("fanout".equalsIgnoreCase(type)) {
                exchange = new FanoutExchange(exchangeName, durable, false);
            } else if ("topic".equalsIgnoreCase(type)) {
                exchange = new TopicExchange(exchangeName, durable, false);
            } else if ("headers".equalsIgnoreCase(type)) {
                exchange = new HeadersExchange(exchangeName, durable, false);
            } else {
                exchange = new DirectExchange(exchangeName, durable, false);
            }
            rabbitAdmin.declareExchange(exchange);
            log.info("[✓] Exchange creado exitosamente: {} (tipo: {})", exchangeName, type);
        } catch (Exception e) {
            log.error("[✗] Error al crear exchange {}: {}", exchangeName, e.getMessage());
            throw new RuntimeException("Error al declarar exchange en RabbitMQ: " + e.getMessage(), e);
        }
    }

    public void createExchange(String exchangeName) {
        createExchange(exchangeName, "direct", true);
    }

    public void createBinding(String queueName, String exchangeName, String routingKey) {
        try {
            Binding binding = BindingBuilder
                    .bind(new Queue(queueName))
                    .to(new DirectExchange(exchangeName))
                    .with(routingKey);
            rabbitAdmin.declareBinding(binding);
            log.info("[✓] Binding creado exitosamente: {} <-> {} (routingKey: {})", queueName, exchangeName, routingKey);
        } catch (Exception e) {
            log.error("[✗] Error al crear binding: {}", e.getMessage());
            throw new RuntimeException("Error al declarar binding en RabbitMQ: " + e.getMessage(), e);
        }
    }

    public QueueInformation getQueueInfo(String queueName) {
        try {
            QueueInformation info = rabbitAdmin.getQueueInfo(queueName);
            if (info != null) {
                log.info("Info de Cola '{}': Mensajes={}, Consumidores={}",
                        info.getName(), info.getMessageCount(), info.getConsumerCount());
            }
            return info;
        } catch (Exception e) {
            log.error("Error al obtener información de la cola {}: {}", queueName, e.getMessage());
            return null;
        }
    }

    public boolean deleteQueue(String queueName) {
        try {
            boolean result = rabbitAdmin.deleteQueue(queueName);
            log.info("[✓] Cola eliminada: {} (resultado: {})", queueName, result);
            return result;
        } catch (Exception e) {
            log.error("[✗] Error al eliminar cola {}: {}", queueName, e.getMessage());
            throw new RuntimeException("Error al eliminar cola en RabbitMQ: " + e.getMessage(), e);
        }
    }

    public boolean deleteExchange(String exchangeName) {
        try {
            boolean result = rabbitAdmin.deleteExchange(exchangeName);
            log.info("[✓] Exchange eliminado: {} (resultado: {})", exchangeName, result);
            return result;
        } catch (Exception e) {
            log.error("[✗] Error al eliminar exchange {}: {}", exchangeName, e.getMessage());
            throw new RuntimeException("Error al eliminar exchange en RabbitMQ: " + e.getMessage(), e);
        }
    }

    public void deleteBinding(String queueName, String exchangeName, String routingKey) {
        try {
            Binding binding = BindingBuilder
                    .bind(new Queue(queueName))
                    .to(new DirectExchange(exchangeName))
                    .with(routingKey);
            rabbitAdmin.removeBinding(binding);
            log.info("[✓] Binding eliminado: {} <-> {}", queueName, exchangeName);
        } catch (Exception e) {
            log.error("[✗] Error al eliminar binding: {}", e.getMessage());
            throw new RuntimeException("Error al eliminar binding en RabbitMQ: " + e.getMessage(), e);
        }
    }

    public int purgeQueue(String queueName) {
        try {
            int purged = rabbitAdmin.purgeQueue(queueName);
            log.info("[✓] Cola purgada: {}, mensajes eliminados: {}", queueName, purged);
            return purged;
        } catch (Exception e) {
            log.error("[✗] Error al purgar cola {}: {}", queueName, e.getMessage());
            throw new RuntimeException("Error al purgar cola en RabbitMQ: " + e.getMessage(), e);
        }
    }
}
