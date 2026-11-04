package cl.duoc.telemedicina.notificaciones.service;

import cl.duoc.telemedicina.notificaciones.dto.AlertaClinicaDTO;
import cl.duoc.telemedicina.notificaciones.dto.CitaEventoDTO;
import cl.duoc.telemedicina.notificaciones.entity.Notificacion;
import cl.duoc.telemedicina.notificaciones.repository.NotificacionRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests Unitarios para AlertaKafkaConsumer
 * Valida que los eventos de Kafka de telemedicina generen notificaciones en BD y se publiquen a Kafka.
 */
class AlertaKafkaConsumerTest {

    private NotificacionRepository notificacionRepository;
    private NotificacionKafkaProducer notificacionKafkaProducer;
    private AlertaKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        notificacionRepository = mock(NotificacionRepository.class);
        notificacionKafkaProducer = mock(NotificacionKafkaProducer.class);
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });
        consumer = new AlertaKafkaConsumer(notificacionRepository, notificacionKafkaProducer);
    }

    @Test
    @DisplayName("Debe procesar alerta clínica de Kafka y guardar notificación SMS en BD")
    void debeProcesarAlertaClinicaYGuardarNotificacion() {
        AlertaClinicaDTO alerta = AlertaClinicaDTO.builder()
                .alertaId("alt-123")
                .tipo(AlertaClinicaDTO.TIPO_CANCELADA)
                .severidad(AlertaClinicaDTO.SEVERIDAD_ALTA)
                .rutPaciente("12.345.678-9")
                .citaId(42L)
                .mensaje("Cita cancelada con Dr. Silva")
                .build();

        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
                "telemedicina.alertas", 0, 10L, "12.345.678-9", alerta);

        consumer.recibirAlertaClinica(record);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(1)).save(captor.capture());
        verify(notificacionKafkaProducer, times(1)).publicarNotificacion(any(Notificacion.class), eq("ALERTA_KAFKA"));

        Notificacion guardada = captor.getValue();
        assertEquals("12.345.678-9", guardada.getRutPaciente());
        assertEquals(42L, guardada.getCitaId());
        assertEquals(Notificacion.TipoNotificacion.SMS, guardada.getTipo());
        assertTrue(guardada.getMensaje().contains("Cita cancelada con Dr. Silva"));
        assertEquals(Notificacion.EstadoEnvio.ENVIADO, guardada.getEstado());
    }

    @Test
    @DisplayName("Debe ignorar alerta si el RUT del paciente es nulo")
    void debeIgnorarAlertaConRutNulo() {
        AlertaClinicaDTO alertaIncompleta = AlertaClinicaDTO.builder()
                .alertaId("alt-bad")
                .mensaje("Sin paciente")
                .build();

        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
                "telemedicina.alertas", 0, 11L, null, alertaIncompleta);

        consumer.recibirAlertaClinica(record);

        verify(notificacionRepository, never()).save(any());
        verify(notificacionKafkaProducer, never()).publicarNotificacion(any(), any());
    }

    @Test
    @DisplayName("Debe generar recordatorio Anti No-Show al recibir evento CITA_CREADA de Kafka")
    void debeGenerarRecordatorioAntiNoShowAlCrearCita() {
        CitaEventoDTO evento = CitaEventoDTO.builder()
                .eventId("evt-001")
                .eventType(CitaEventoDTO.EVENTO_CREADA)
                .citaId(99L)
                .rutPaciente("98.765.432-1")
                .nombreMedico("Dr. Roberto Silva")
                .fechaHora("mañana a las 10:30 hrs")
                .build();

        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
                "telemedicina.citas.eventos", 0, 12L, "98.765.432-1", evento);

        consumer.recibirEventoCita(record);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(1)).save(captor.capture());
        verify(notificacionKafkaProducer, times(1)).publicarNotificacion(any(Notificacion.class), eq("RECORDATORIO_AUTO"));

        Notificacion guardada = captor.getValue();
        assertEquals("98.765.432-1", guardada.getRutPaciente());
        assertEquals(99L, guardada.getCitaId());
        assertEquals(Notificacion.TipoNotificacion.SMS, guardada.getTipo());
        assertTrue(guardada.getMensaje().contains("Recordatorio Anti No-Show"));
    }
}
