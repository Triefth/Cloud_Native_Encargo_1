package cl.duoc.telemedicina.notificaciones.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ========== GUÍA 1 & 2: LOGGING & DIRECT EXCHANGE ==========
    public static final String EXCHANGE_NAME = "logs_direct_exchange";
    public static final String ALL_LOGS_QUEUE = "all_logs_queue";
    public static final String ERRORS_ONLY_QUEUE = "errors_only_queue";
    public static final String HELLO_QUEUE = "hello";

    // ========== GUÍA 3: DLX / DLQ & ORDERS ==========
    public static final String ORDERS_EXCHANGE = "orders.exchange";
    public static final String ORDERS_QUEUE = "orders.queue";
    public static final String ORDERS_ROUTING_KEY = "order.created";

    public static final String DLX_EXCHANGE = "orders.dlx";
    public static final String DLQ_QUEUE = "orders.dlq";
    public static final String DLX_ROUTING_KEY = "order.dead";

    // --- GUÍA 1 & 2 BEANS ---
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue allLogsQueue() {
        return new Queue(ALL_LOGS_QUEUE, true);
    }

    @Bean
    public Queue errorsOnlyQueue() {
        return new Queue(ERRORS_ONLY_QUEUE, true);
    }

    @Bean
    public Queue helloQueue() {
        return new Queue(HELLO_QUEUE, false);
    }

    @Bean
    public Binding bindAllLogsForInfo() {
        return BindingBuilder.bind(allLogsQueue()).to(directExchange()).with("INFO");
    }

    @Bean
    public Binding bindAllLogsForWarning() {
        return BindingBuilder.bind(allLogsQueue()).to(directExchange()).with("WARNING");
    }

    @Bean
    public Binding bindAllLogsForError() {
        return BindingBuilder.bind(allLogsQueue()).to(directExchange()).with("ERROR");
    }

    @Bean
    public Binding bindErrorsOnly() {
        return BindingBuilder.bind(errorsOnlyQueue()).to(directExchange()).with("ERROR");
    }

    // --- GUÍA 3 BEANS: ORDERS EXCHANGE & DLQ ---
    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(ORDERS_EXCHANGE, true, false);
    }

    @Bean
    public Queue ordersQueue() {
        return QueueBuilder.durable(ORDERS_QUEUE)
                .withArgument("x-message-ttl", 30000) // TTL 30s
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
                .withArgument("x-max-length", 1000)
                .build();
    }

    @Bean
    public Binding ordersBinding() {
        return BindingBuilder.bind(ordersQueue())
                .to(ordersExchange())
                .with(ORDERS_ROUTING_KEY);
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return new FanoutExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE)
                .withArgument("x-message-ttl", 86400000) // 24h TTL
                .build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange());
    }
}
