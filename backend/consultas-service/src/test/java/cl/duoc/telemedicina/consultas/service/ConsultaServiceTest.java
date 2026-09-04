package cl.duoc.telemedicina.consultas.service;

import cl.duoc.telemedicina.consultas.entity.Consulta;
import cl.duoc.telemedicina.consultas.entity.Consulta.EstadoConsulta;
import cl.duoc.telemedicina.consultas.repository.ConsultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private ConsultaService consultaService;

    private Consulta sampleConsulta;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleConsulta = Consulta.builder()
                .id(1L)
                .citaId(10L)
                .rutPaciente("12345678-9")
                .rutMedico("98765432-1")
                .estado(EstadoConsulta.EN_CURSO)
                .build();
    }

    @Test
    void testIniciarConsulta() {
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Consulta result = consultaService.iniciarConsulta(10L, "12345678-9", "98765432-1");
        assertNotNull(result);
        assertEquals(EstadoConsulta.EN_CURSO, result.getEstado());
        assertTrue(result.getCpaasJoinUrl().contains("meet.telemedicina-rural.cl"));
    }

    @Test
    void testFinalizarConsulta() {
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(sampleConsulta));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Consulta result = consultaService.finalizarConsulta(1L, "Gripe común", "Reposo 3 días", 20);
        assertEquals(EstadoConsulta.FINALIZADA, result.getEstado());
        assertEquals("Gripe común", result.getDiagnosticoPreliminar());
        assertEquals(20, result.getDuracionMinutos());
    }
}
