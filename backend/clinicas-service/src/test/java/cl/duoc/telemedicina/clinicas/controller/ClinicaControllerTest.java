package cl.duoc.telemedicina.clinicas.controller;

import cl.duoc.telemedicina.clinicas.entity.ClinicaRural;
import cl.duoc.telemedicina.clinicas.service.ClinicaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ClinicaControllerTest {

    @Mock
    private ClinicaService clinicaService;

    @InjectMocks
    private ClinicaController clinicaController;

    private ClinicaRural sampleClinica;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleClinica = ClinicaRural.builder()
                .id(1L)
                .nombre("Clínica Rural San Pedro")
                .comuna("San Pedro de Atacama")
                .build();
    }

    @Test
    void testObtenerTodas() {
        when(clinicaService.obtenerTodas()).thenReturn(List.of(sampleClinica));
        ResponseEntity<List<ClinicaRural>> response = clinicaController.obtenerTodas();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testObtenerPorIdExistente() {
        when(clinicaService.obtenerPorId(1L)).thenReturn(Optional.of(sampleClinica));
        ResponseEntity<?> response = clinicaController.obtenerPorId(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testObtenerPorIdNoExistente() {
        when(clinicaService.obtenerPorId(99L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = clinicaController.obtenerPorId(99L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
