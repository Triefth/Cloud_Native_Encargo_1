package cl.duoc.telemedicina.citas.service;

import cl.duoc.telemedicina.citas.dto.CitaEventoDTO;
import cl.duoc.telemedicina.citas.entity.Cita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests Unitarios del CitaEventoKafkaProducer (EP3 - citas-service como productor).
 * Verifica que al publicar un evento se construya correctamente el CitaEventoDTO
 * y se envíe al tópico correcto usando la especialidad del médico como clave de partición.
 */
class CitaEventoKafkaProducerTest {

    private KafkaTemplate<String, CitaEventoDTO> kafkaTemplate;
    private CitaEventoKafkaProducer producer;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        producer = new CitaEventoKafkaProducer(kafkaTemplate);
        ReflectionTestUtils.setField(producer, "topicCitasEventos", "telemedicina.citas.eventos");
    }

    private Cita cita() {
        Cita cita = new Cita();
        cita.setId(42L);
        cita.setRutPaciente("12.345.678-9");
        cita.setNombrePaciente("María González");
        cita.setRutMedico("11.111.111-1");
        cita.setNombreMedico("Dr. Pérez");
        cita.setEspecialidad("Medicina General");
        cita.setFechaHora(LocalDateTime.of(2026, 11, 5, 10, 30));
        cita.setMotivoConsulta("Control de hipertensión");
        cita.setClinicaRural("Clínica Rural San Pedro");
        return cita;
    }

    @Test
    @DisplayName("1.1 - publicarEvento envía al tópico telemedicina.citas.eventos con la clave = especialidad médica")
    void publicarEventoEnviaAlTopicCorrectoConClaveEspecialidad() {
        producer.publicarEvento(cita(), CitaEventoDTO.EVENTO_CREADA, "PROGRAMADA");

        verify(kafkaTemplate, times(1))
                .send(eq("telemedicina.citas.eventos"), eq("Medicina General"), any(CitaEventoDTO.class));
    }

    @Test
    @DisplayName("1.2 - publicarEvento construye el DTO con los datos de la cita")
    void publicarEventoConstruyeDto() {
        ArgumentCaptor<CitaEventoDTO> captor = ArgumentCaptor.forClass(CitaEventoDTO.class);

        producer.publicarEvento(cita(), CitaEventoDTO.EVENTO_CANCELADA, "CANCELADA");

        verify(kafkaTemplate).send(eq("telemedicina.citas.eventos"), eq("Medicina General"), captor.capture());
        CitaEventoDTO dto = captor.getValue();

        assertEquals("CITA_CANCELADA", dto.getEventType());
        assertEquals(42L, dto.getCitaId());
        assertEquals("12.345.678-9", dto.getRutPaciente());
        assertEquals("María González", dto.getNombrePaciente());
        assertEquals("Medicina General", dto.getEspecialidad());
        assertEquals("CANCELADA", dto.getEstado());
        assertNotNull(dto.getEventId(), "Debe generar un eventId único");
        assertNotNull(dto.getTimestamp(), "Debe incluir timestamp");
    }

    @Test
    @DisplayName("1.3 - publicarEvento rellena los campos de auditoría (eventId, timestamp)")
    void publicarEventoRellenaAuditoria() {
        ArgumentCaptor<CitaEventoDTO> captor = ArgumentCaptor.forClass(CitaEventoDTO.class);

        producer.publicarEvento(cita(), CitaEventoDTO.EVENTO_REPROGRAMADA, "REPROGRAMADA");

        verify(kafkaTemplate).send(eq("telemedicina.citas.eventos"), eq("Medicina General"), captor.capture());
        CitaEventoDTO dto = captor.getValue();

        assertFalse(dto.getEventId().isBlank(), "eventId no debe estar vacío");
        assertFalse(dto.getTimestamp().isBlank(), "timestamp no debe estar vacío");
        assertEquals("2026-11-05T10:30:00", dto.getFechaHora(), "fechaHora debe estar en formato ISO-8601");
    }

    @Test
    @DisplayName("1.4 - Si especialidad es nula o vacía, usa fallback MEDICINA_GENERAL como key")
    void publicarEventoUsaFallbackCuandoEspecialidadEsNula() {
        Cita citaSinEsp = cita();
        citaSinEsp.setEspecialidad(null);

        producer.publicarEvento(citaSinEsp, CitaEventoDTO.EVENTO_CREADA, "PROGRAMADA");

        verify(kafkaTemplate).send(eq("telemedicina.citas.eventos"), eq("MEDICINA_GENERAL"), any(CitaEventoDTO.class));
    }
}
