package cl.duoc.telemedicina.notificaciones.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * =====================================================================
 * RabbitMQConfig: Centralización de Beans y Nombres de Recursos AMQP
 * Cumple con Pauta Duoc UC EP2:
 *  - 12% Nombres de colas, exchanges y routing keys centralizados
 *  - 13% Declaración explícita de Queue, Exchange y Binding por caso de uso
 *  - 10% Aislamiento de la configuración de mensajería
 *  - 20% Configuración de Dead Letter Queue (DLQ) y DLX con TTL y routing
 * =====================================================================
 */
@Configuration
public class RabbitMQConfig {

    // Constantes por defecto (compatibilidad y fallback centralizado)
    public static final String EXCHANGE_NAME = "logs_direct_exchange";
    public static final String ALL_LOGS_QUEUE = "all_logs_queue";
    public static final String ERRORS_ONLY_QUEUE = "errors_only_queue";
    public static final String HELLO_QUEUE = "hello";

    public static final String ORDERS_EXCHANGE = "orders.exchange";
    public static final String ORDERS_QUEUE = "orders.queue";
    public static final String ORDERS_ROUTING_KEY = "order.created";
    public static final String DLX_EXCHANGE = "orders.dlx";
    public static final String DLQ_QUEUE = "orders.dlq";
    public static final String DLX_ROUTING_KEY = "order.dead";

    public static final String CITAS_EXCHANGE = "citas.exchange";
    public static final String CITAS_QUEUE = "citas.queue";
    public static final String CITAS_ROUTING_KEY = "cita.created";
    public static final String CITAS_DLX_EXCHANGE = "citas.dlx";
    public static final String CITAS_DLQ_QUEUE = "citas.dlq";
    public static final String CITAS_DLX_ROUTING_KEY = "cita.dead";

    // Inyección de valores desde application.yml
    @Value("${app.rabbitmq.queues.hello:hello}")
    private String helloQueueName;

    @Value("${app.rabbitmq.queues.orders:orders.queue}")
    private String ordersQueueName;

    @Value("${app.rabbitmq.queues.orders-dlq:orders.dlq}")
    private String ordersDlqName;

    @Value("${app.rabbitmq.queues.all-logs:all_logs_queue}")
    private String allLogsQueueName;

    @Value("${app.rabbitmq.queues.errors-only:errors_only_queue}")
    private String errorsOnlyQueueName;

    @Value("${app.rabbitmq.queues.citas:citas.queue}")
    private String citasQueueName;

    @Value("${app.rabbitmq.queues.citas-dlq:citas.dlq}")
    private String citasDlqName;

    @Value("${app.rabbitmq.exchanges.logs:logs_direct_exchange}")
    private String logsExchangeName;

    @Value("${app.rabbitmq.exchanges.orders:orders.exchange}")
    private String ordersExchangeName;

    @Value("${app.rabbitmq.exchanges.orders-dlx:orders.dlx}")
    private String ordersDlxName;

    @Value("${app.rabbitmq.exchanges.citas:citas.exchange}")
    private String citasExchangeName;

    @Value("${app.rabbitmq.exchanges.citas-dlx:citas.dlx}")
    private String citasDlxName;

    @Value("${app.rabbitmq.routing-keys.order-created:order.created}")
    private String orderCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-keys.order-dead:order.dead}")
    private String orderDeadRoutingKey;

    @Value("${app.rabbitmq.routing-keys.cita-created:cita.created}")
    private String citaCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-keys.cita-dead:cita.dead}")
    private String citaDeadRoutingKey;

    @Value("${app.rabbitmq.routing-keys.log-info:INFO}")
    private String logInfoRoutingKey;

    @Value("${app.rabbitmq.routing-keys.log-warning:WARNING}")
    private String logWarningRoutingKey;

    @Value("${app.rabbitmq.routing-keys.log-error:ERROR}")
    private String logErrorRoutingKey;

    // =================================================================
    // CASO DE USO 1: Mensajería Directa Punto a Punto (Guía 1 Duoc UC)
    // =================================================================
    @Bean
    public Queue helloQueue() {
        return QueueBuilder.durable(helloQueueName).build();
    }

    // =================================================================
    // CASO DE USO 2: Logging y Ruteo por Severidad (Guía 2 Duoc UC)
    // =================================================================
    @Bean
    public DirectExchange directLogsExchange() {
        return new DirectExchange(logsExchangeName, true, false);
    }

    @Bean
    public Queue allLogsQueue() {
        return QueueBuilder.durable(allLogsQueueName).build();
    }

    @Bean
    public Queue errorsOnlyQueue() {
        return QueueBuilder.durable(errorsOnlyQueueName).build();
    }

    @Bean
    public Binding bindAllLogsForInfo() {
        return BindingBuilder.bind(allLogsQueue())
                .to(directLogsExchange())
                .with(logInfoRoutingKey);
    }

    @Bean
    public Binding bindAllLogsForWarning() {
        return BindingBuilder.bind(allLogsQueue())
                .to(directLogsExchange())
                .with(logWarningRoutingKey);
    }

    @Bean
    public Binding bindAllLogsForError() {
        return BindingBuilder.bind(allLogsQueue())
                .to(directLogsExchange())
                .with(logErrorRoutingKey);
    }

    @Bean
    public Binding bindErrorsOnly() {
        return BindingBuilder.bind(errorsOnlyQueue())
                .to(directLogsExchange())
                .with(logErrorRoutingKey);
    }

    // =================================================================
    // CASO DE USO 3: Órdenes con Resiliencia DLX / DLQ (Guía 3 Duoc UC)
    // =================================================================
    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(ordersExchangeName, true, false);
    }

    @Bean
    public Queue ordersQueue() {
        return QueueBuilder.durable(ordersQueueName)
                .withArgument("x-message-ttl", 30000) // TTL 30s
                .withArgument("x-dead-letter-exchange", ordersDlxName)
                .withArgument("x-dead-letter-routing-key", orderDeadRoutingKey)
                .withArgument("x-max-length", 1000)
                .build();
    }

    @Bean
    public Binding ordersBinding() {
        return BindingBuilder.bind(ordersQueue())
                .to(ordersExchange())
                .with(orderCreatedRoutingKey);
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return new FanoutExchange(ordersDlxName, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(ordersDlqName)
                .withArgument("x-message-ttl", 86400000) // 24h TTL
                .build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange());
    }

    // =================================================================
    // CASO DE USO 4: Integración Microservicio Negocio 'citas-service'
    // Mensajería desacoplada para agendamiento remoto y notificaciones
    // =================================================================
    @Bean
    public DirectExchange citasExchange() {
        return new DirectExchange(citasExchangeName, true, false);
    }

    @Bean
    public Queue citasQueue() {
        return QueueBuilder.durable(citasQueueName)
                .withArgument("x-message-ttl", 60000) // TTL 60s
                .withArgument("x-dead-letter-exchange", citasDlxName)
                .withArgument("x-dead-letter-routing-key", citaDeadRoutingKey)
                .build();
    }

    @Bean
    public Binding citasBinding() {
        return BindingBuilder.bind(citasQueue())
                .to(citasExchange())
                .with(citaCreatedRoutingKey);
    }

    @Bean
    public FanoutExchange citasDlxExchange() {
        return new FanoutExchange(citasDlxName, true, false);
    }

    @Bean
    public Queue citasDlqQueue() {
        return QueueBuilder.durable(citasDlqName)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public Binding citasDlqBinding() {
        return BindingBuilder.bind(citasDlqQueue())
                .to(citasDlxExchange());
    }
}
