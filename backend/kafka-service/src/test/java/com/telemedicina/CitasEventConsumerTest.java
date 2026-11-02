package com.telemedicina;

import com.telemedicina.dto.AlertaClinicaDTO;
import com.telemedicina.dto.CitaEventoDTO;
import com.telemedicina.service.AlertaProducer;
import com.telemedicina.service.CitasEventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests Unitarios del CitasEventConsumer (EP3 - Dominio Telemedicina).
 * Valida la lógica de negocio de detección de alertas a partir de eventos de citas,
 * separada de la infraestructura de mensajería.
 */
class CitasEventConsumerTest {

    private CitasEventConsumer consumer;

    @BeforeEach
    void setUp() {
        // AlertaProducer mockeado: el foco del test es la decisión de negocio (¿generar alerta?)
        consumer = new CitasEventConsumer(mock(AlertaProducer.class));
    }

    private CitaEventoDTO evento(String eventType) {
        return CitaEventoDTO.builder()
                .eventId("evt-1")
                .eventType(eventType)
                .citaId(42L)
                .rutPaciente("12.345.678-9")
                .nombrePaciente("María González")
                .rutMedico("11.111.111-1")
                .nombreMedico("Dr. Pérez")
                .estado(eventType.replace("CITA_", ""))
                .fechaHora("2026-11-05T10:30:00")
                .timestamp("2026-11-02T17:00:00Z")
                .build();
    }

    @Test
    @DisplayName("1.1 - CITA_CREADA no genera alerta clínica")
    void citaCreadaNoGeneraAlerta() {
        Optional<AlertaClinicaDTO> alerta = consumer.procesarEvento(evento(CitaEventoDTO.EVENTO_CREADA));
        assertTrue(alerta.isEmpty(), "Una cita creada no debe generar alerta");
    }

    @Test
    @DisplayName("1.2 - CITA_CONFIRMADA no genera alerta clínica")
    void citaConfirmadaNoGeneraAlerta() {
        Optional<AlertaClinicaDTO> alerta = consumer.procesarEvento(evento(CitaEventoDTO.EVENTO_CONFIRMADA));
        assertTrue(alerta.isEmpty(), "Una cita confirmada no debe generar alerta");
    }

    @Test
    @DisplayName("2.1 - CITA_CANCELADA genera alerta de severidad ALTA")
    void citaCanceladaGeneraAlertaAlta() {
        Optional<AlertaClinicaDTO> alerta = consumer.procesarEvento(evento(CitaEventoDTO.EVENTO_CANCELADA));

        assertTrue(alerta.isPresent(), "Una cita cancelada debe generar alerta");
        assertEquals(AlertaClinicaDTO.TIPO_CANCELADA, alerta.get().getTipo());
        assertEquals(AlertaClinicaDTO.SEVERIDAD_ALTA, alerta.get().getSeveridad());
        assertEquals("12.345.678-9", alerta.get().getRutPaciente());
        assertEquals(42L, alerta.get().getCitaId());
        assertEquals("evt-1", alerta.get().getSourceEventId(), "Debe conservar trazabilidad hacia el evento origen");
        assertNotNull(alerta.get().getAlertaId(), "La alerta debe tener un id único");
        assertNotNull(alerta.get().getTimestamp(), "La alerta debe tener timestamp");
    }

    @Test
    @DisplayName("2.2 - CITA_REPROGRAMADA genera alerta de severidad MEDIA")
    void citaReprogramadaGeneraAlertaMedia() {
        Optional<AlertaClinicaDTO> alerta = consumer.procesarEvento(evento(CitaEventoDTO.EVENTO_REPROGRAMADA));

        assertTrue(alerta.isPresent(), "Una cita reprogramada debe generar alerta");
        assertEquals(AlertaClinicaDTO.TIPO_REPROGRAMADA, alerta.get().getTipo());
        assertEquals(AlertaClinicaDTO.SEVERIDAD_MEDIA, alerta.get().getSeveridad());
        assertTrue(alerta.get().getMensaje().contains("reprogramada"),
                "El mensaje debe indicar que la cita fue reprogramada");
    }

    @Test
    @DisplayName("3.1 - getReceivedEvents inicia vacía")
    void listaEventosVaciaAlInicio() {
        assertNotNull(consumer.getReceivedEvents());
        assertTrue(consumer.getReceivedEvents().isEmpty());
    }

    @Test
    @DisplayName("3.2 - clearEvents vacía la lista de eventos recibidos")
    void clearEventsVaciaLista() {
        consumer.clearEvents();
        assertTrue(consumer.getReceivedEvents().isEmpty());
    }
}
