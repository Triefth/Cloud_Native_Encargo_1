package cl.duoc.telemedicina.consultas.controller;

import cl.duoc.telemedicina.consultas.entity.Consulta;
import cl.duoc.telemedicina.consultas.service.ConsultaService;
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

class ConsultaControllerTest {

    @Mock
    private ConsultaService consultaService;

    @InjectMocks
    private ConsultaController consultaController;

    private Consulta sampleConsulta;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleConsulta = Consulta.builder()
                .id(1L)
                .citaId(10L)
                .rutPaciente("12345678-9")
                .rutMedico("98765432-1")
                .build();
    }

    @Test
    void testObtenerTodas() {
        when(consultaService.obtenerTodas()).thenReturn(List.of(sampleConsulta));
        ResponseEntity<List<Consulta>> response = consultaController.obtenerTodas();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testObtenerPorId() {
        when(consultaService.obtenerPorId(1L)).thenReturn(Optional.of(sampleConsulta));
        ResponseEntity<?> response = consultaController.obtenerPorId(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
