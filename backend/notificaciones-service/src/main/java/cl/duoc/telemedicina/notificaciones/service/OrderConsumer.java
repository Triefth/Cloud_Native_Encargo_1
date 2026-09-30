package cl.duoc.telemedicina.notificaciones.service;

import cl.duoc.telemedicina.notificaciones.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    /**
     * Consumidor principal: Recibe órdenes de la cola principal.
     * Simula fallos aleatorios para demostrar el DLX.
     */
    @RabbitListener(id = "order-listener", queues = RabbitMQConfig.ORDERS_QUEUE, containerFactory = "orderListenerFactory")
    public void processOrder(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        try {
            System.out.println("[PROCESADOR] Recibida orden: " + message);

            // SIMULACIÓN DE FALLO ALEATORIO (50% probabilidad de fallar)
            if (Math.random() < 0.5) {
                throw new RuntimeException("Error simulado al procesar: " + message);
            }

            // Procesamiento exitoso
            System.out.println("[✓ ÉXITO] Orden procesada correctamente: " + message);
            // Ack manual: Le decimos a RabbitMQ que todo estuvo bien
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            System.out.println("[⚠️ ERROR] " + e.getMessage());
            try {
                // Nack con requeue=false: Va al DLX, no se reintenta aquí en la cola principal
                channel.basicNack(deliveryTag, false, false);
                System.out.println("[→ DLX] Mensaje enviado a Dead Letter Exchange");
            } catch (Exception nackException) {
                nackException.printStackTrace();
            }
        }
    }

    /**
     * Consumidor de DLQ: Monitorea los mensajes que fallaron definitivamente.
     */
    @RabbitListener(queues = RabbitMQConfig.DLQ_QUEUE)
    public void processDLQ(String message) {
        System.out.println("🚨 [DLQ] MENSAJE EN CUARENTENA: " + message);
        System.out.println("   → Revisar logs del procesador para diagrama del fallo.");
    }
}
