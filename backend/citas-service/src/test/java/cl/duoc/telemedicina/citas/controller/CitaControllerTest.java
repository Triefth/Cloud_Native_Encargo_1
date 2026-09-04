package cl.duoc.telemedicina.citas.controller;

import cl.duoc.telemedicina.citas.entity.Cita;
import cl.duoc.telemedicina.citas.service.CitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CitaControllerTest {

    @Mock
    private CitaService citaService;

    @InjectMocks
    private CitaController citaController;

    private Cita sampleCita;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleCita = Cita.builder()
                .id(1L)
                .rutPaciente("12345678-9")
                .nombrePaciente("Juan Perez")
                .rutMedico("98765432-1")
                .nombreMedico("Dr. Soto")
                .fechaHora(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    void testObtenerTodas() {
        when(citaService.obtenerTodas()).thenReturn(List.of(sampleCita));
        ResponseEntity<List<Cita>> response = citaController.obtenerTodas();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testObtenerPorIdExistente() {
        when(citaService.obtenerPorId(1L)).thenReturn(Optional.of(sampleCita));
        ResponseEntity<?> response = citaController.obtenerPorId(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testObtenerPorIdNoExistente() {
        when(citaService.obtenerPorId(99L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = citaController.obtenerPorId(99L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
