package cl.duoc.telemedicina.citas.service;

import cl.duoc.telemedicina.citas.entity.Cita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * =====================================================================
 * CitaEventProducer: Productor Asíncrono de Eventos de Citas Médicas
 * Cumple con Pauta Duoc UC EP2:
 *  - 15% Productores y consumidores en microservicios de negocio
 *  - 12% Nombres centralizados inyectados desde application.yml
 *  - 10% Lógica de negocio aislada de la infraestructura de mensajería
 * =====================================================================
 */
@Service
public class CitaEventProducer {

    private static final Logger log = LoggerFactory.getLogger(CitaEventProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public CitaEventProducer() {
    }

    public CitaEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${app.rabbitmq.exchanges.citas:citas.exchange}")
    private String citasExchange;

    @Value("${app.rabbitmq.routing-keys.cita-created:cita.created}")
    private String citaCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-keys.cita-status:cita.status}")
    private String citaStatusRoutingKey;

    /**
     * Publica evento asíncrono al agendar una nueva cita médica remota.
     */
    public void notificarCitaCreada(Cita cita) {
        try {
            String payload = String.format(
                    "{\"evento\":\"CITA_CREADA\",\"citaId\":%d,\"rutPaciente\":\"%s\",\"rutMedico\":\"%s\",\"fechaHora\":\"%s\",\"estado\":\"%s\",\"timestamp\":\"%s\"}",
                    cita.getId(),
                    cita.getRutPaciente(),
                    cita.getRutMedico(),
                    cita.getFechaHora(),
                    cita.getEstado(),
                    LocalDateTime.now()
            );

            log.info("[AMQP PRODUCTOR CITAS] Publicando evento CITA_CREADA hacia exchange='{}', routingKey='{}'",
                    citasExchange, citaCreatedRoutingKey);

            rabbitTemplate.convertAndSend(citasExchange, citaCreatedRoutingKey, payload);
            log.info("[✓] Evento CITA_CREADA publicado exitosamente en RabbitMQ: {}", payload);

        } catch (Exception e) {
            log.error("[✗] Error al publicar evento de cita creada a RabbitMQ: {}", e.getMessage());
        }
    }

    /**
     * Publica evento asíncrono cuando cambia el estado de una cita médica (confirmada, cancelada, reprogramada).
     */
    public void notificarCambioEstado(Cita cita, String nuevoEstado) {
        try {
            String payload = String.format(
                    "{\"evento\":\"CITA_ESTADO_ACTUALIZADO\",\"citaId\":%d,\"rutPaciente\":\"%s\",\"nuevoEstado\":\"%s\",\"timestamp\":\"%s\"}",
                    cita.getId(),
                    cita.getRutPaciente(),
                    nuevoEstado,
                    LocalDateTime.now()
            );

            log.info("[AMQP PRODUCTOR CITAS] Publicando actualización de estado hacia exchange='{}'", citasExchange);
            rabbitTemplate.convertAndSend(citasExchange, citaStatusRoutingKey, payload);

        } catch (Exception e) {
            log.error("[✗] Error al publicar cambio de estado de cita: {}", e.getMessage());
        }
    }
}
