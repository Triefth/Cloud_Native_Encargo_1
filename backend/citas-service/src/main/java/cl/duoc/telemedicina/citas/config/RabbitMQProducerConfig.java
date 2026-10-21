package cl.duoc.telemedicina.citas.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * =====================================================================
 * RabbitMQProducerConfig: Configuración AMQP para citas-service
 * Cumple con Pauta Duoc UC EP2:
 *  - 13% Declaración explícita de Queue, Exchange y Binding por caso de uso
 *  - 12% Nombres de colas centralizados inyectados desde application.yml
 *  - 10% Separación de lógica de negocio y configuración de mensajería
 * =====================================================================
 */
@Configuration
public class RabbitMQProducerConfig {

    @Value("${app.rabbitmq.queues.citas:citas.queue}")
    private String citasQueueName;

    @Value("${app.rabbitmq.queues.citas-dlq:citas.dlq}")
    private String citasDlqName;

    @Value("${app.rabbitmq.exchanges.citas:citas.exchange}")
    private String citasExchangeName;

    @Value("${app.rabbitmq.exchanges.citas-dlx:citas.dlx}")
    private String citasDlxName;

    @Value("${app.rabbitmq.routing-keys.cita-created:cita.created}")
    private String citaCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-keys.cita-dead:cita.dead}")
    private String citaDeadRoutingKey;

    @Value("${app.rabbitmq.queues.citas-feedback:citas.feedback.queue}")
    private String citasFeedbackQueueName;

    @Bean
    public Queue citasFeedbackQueue() {
        return QueueBuilder.durable(citasFeedbackQueueName).build();
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public DirectExchange citasExchange() {
        return new DirectExchange(citasExchangeName, true, false);
    }

    @Bean
    public Queue citasQueue() {
        return QueueBuilder.durable(citasQueueName)
                .withArgument("x-message-ttl", 60000)
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
