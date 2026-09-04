package cl.duoc.telemedicina.reportes.service;

import cl.duoc.telemedicina.reportes.entity.ReporteOperativo;
import cl.duoc.telemedicina.reportes.entity.ReporteOperativo.Severidad;
import cl.duoc.telemedicina.reportes.repository.ReporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @InjectMocks
    private ReporteService reporteService;

    private ReporteOperativo sampleReporte;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleReporte = ReporteOperativo.builder()
                .id(1L)
                .moduloServicio("citas-service")
                .tipoEvento("CREACION_CITA")
                .latenciaMs(45L)
                .severidad(Severidad.INFO)
                .build();
    }

    @Test
    void testGenerarResumenOperativo() {
        when(reporteRepository.findAll()).thenReturn(List.of(sampleReporte));

        Map<String, Object> resumen = reporteService.generarResumenOperativo();
        assertNotNull(resumen);
        assertEquals(1L, resumen.get("totalEventosRegistrados"));
        assertEquals("OPERACIONAL_OPTIMO", resumen.get("estadoPlataforma"));
    }
}
