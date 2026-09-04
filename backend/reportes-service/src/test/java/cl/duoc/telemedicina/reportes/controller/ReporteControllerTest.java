package cl.duoc.telemedicina.reportes.controller;

import cl.duoc.telemedicina.reportes.entity.ReporteOperativo;
import cl.duoc.telemedicina.reportes.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ReporteControllerTest {

    @Mock
    private ReporteService reporteService;

    @InjectMocks
    private ReporteController reporteController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testObtenerResumen() {
        when(reporteService.generarResumenOperativo()).thenReturn(Map.of("estadoPlataforma", "OPERACIONAL_OPTIMO"));
        ResponseEntity<Map<String, Object>> response = reporteController.obtenerResumen();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OPERACIONAL_OPTIMO", response.getBody().get("estadoPlataforma"));
    }
}
