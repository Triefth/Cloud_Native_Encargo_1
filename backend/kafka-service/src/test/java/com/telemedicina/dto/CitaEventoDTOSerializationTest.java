package com.telemedicina.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del contrato JSON (modelo de mensajes entrada/salida) de EP3.
 * Verifica que CitaEventoDTO y AlertaClinicaDTO serialicen con los nombres
 * de campo correctos y que la deserialización sea simétrica.
 */
class CitaEventoDTOSerializationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    @DisplayName("1.1 - CitaEventoDTO serializa con nombres de campo del contrato")
    void citaEventoSerializaContrato() throws Exception {
        CitaEventoDTO evento = CitaEventoDTO.builder()
                .eventId("evt-1")
                .eventType("CITA_CANCELADA")
                .citaId(42L)
                .rutPaciente("12.345.678-9")
                .nombrePaciente("María")
                .rutMedico("11.111.111-1")
                .estado("CANCELADA")
                .fechaHora("2026-11-05T10:30:00")
                .timestamp("2026-11-02T17:00:00Z")
                .build();

        String json = mapper.writeValueAsString(evento);

        assertTrue(json.contains("\"eventId\":\"evt-1\""), "Debe incluir eventId: " + json);
        assertTrue(json.contains("\"eventType\":\"CITA_CANCELADA\""), "Debe incluir eventType: " + json);
        assertTrue(json.contains("\"citaId\":42"), "Debe incluir citaId como número: " + json);
        assertTrue(json.contains("\"rutPaciente\":\"12.345.678-9\""), "Debe incluir rutPaciente: " + json);
        assertTrue(json.contains("\"timestamp\":\"2026-11-02T17:00:00Z\""), "Debe incluir timestamp: " + json);
    }

    @Test
    @DisplayName("1.2 - CitaEventoDTO deserializa de forma simétrica (round-trip)")
    void citaEventoRoundTrip() throws Exception {
        CitaEventoDTO original = CitaEventoDTO.builder()
                .eventId("evt-1")
                .eventType("CITA_CREADA")
                .citaId(7L)
                .rutPaciente("9.999.999-9")
                .nombrePaciente("Juan")
                .rutMedico("8.888.888-8")
                .estado("PROGRAMADA")
                .build();

        String json = mapper.writeValueAsString(original);
        CitaEventoDTO restaurado = mapper.readValue(json, CitaEventoDTO.class);

        assertEquals(original.getEventId(), restaurado.getEventId());
        assertEquals(original.getEventType(), restaurado.getEventType());
        assertEquals(original.getCitaId(), restaurado.getCitaId());
        assertEquals(original.getRutPaciente(), restaurado.getRutPaciente());
        assertEquals(original.getNombrePaciente(), restaurado.getNombrePaciente());
    }

    @Test
    @DisplayName("2.1 - AlertaClinicaDTO serializa con nombres de campo del contrato")
    void alertaSerializaContrato() throws Exception {
        AlertaClinicaDTO alerta = AlertaClinicaDTO.builder()
                .alertaId("al-1")
                .tipo("CITA_CANCELADA")
                .severidad("ALTA")
                .rutPaciente("12.345.678-9")
                .nombrePaciente("María")
                .citaId(42L)
                .mensaje("Se requiere reagendar")
                .sourceEventId("evt-1")
                .timestamp("2026-11-02T17:00:01Z")
                .build();

        String json = mapper.writeValueAsString(alerta);

        assertTrue(json.contains("\"alertaId\":\"al-1\""), "Debe incluir alertaId: " + json);
        assertTrue(json.contains("\"tipo\":\"CITA_CANCELADA\""), "Debe incluir tipo: " + json);
        assertTrue(json.contains("\"severidad\":\"ALTA\""), "Debe incluir severidad: " + json);
        assertTrue(json.contains("\"sourceEventId\":\"evt-1\""), "Debe incluir sourceEventId: " + json);
    }

    @Test
    @DisplayName("2.2 - AlertaClinicaDTO deserializa de forma simétrica (round-trip)")
    void alertaRoundTrip() throws Exception {
        AlertaClinicaDTO original = AlertaClinicaDTO.builder()
                .alertaId("al-1")
                .tipo("REPROGRAMACION")
                .severidad("MEDIA")
                .rutPaciente("12.345.678-9")
                .citaId(42L)
                .sourceEventId("evt-1")
                .build();

        String json = mapper.writeValueAsString(original);
        AlertaClinicaDTO restaurado = mapper.readValue(json, AlertaClinicaDTO.class);

        assertEquals(original.getAlertaId(), restaurado.getAlertaId());
        assertEquals(original.getTipo(), restaurado.getTipo());
        assertEquals(original.getSeveridad(), restaurado.getSeveridad());
        assertEquals(original.getSourceEventId(), restaurado.getSourceEventId());
    }
}
