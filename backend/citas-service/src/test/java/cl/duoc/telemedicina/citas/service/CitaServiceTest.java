package cl.duoc.telemedicina.citas.service;

import cl.duoc.telemedicina.citas.entity.Cita;
import cl.duoc.telemedicina.citas.entity.Cita.EstadoCita;
import cl.duoc.telemedicina.citas.repository.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private CitaService citaService;

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
                .especialidad("Medicina General")
                .fechaHora(LocalDateTime.now().plusDays(1))
                .estado(EstadoCita.PROGRAMADA)
                .build();
    }

    @Test
    void testObtenerTodas() {
        when(citaRepository.findAll()).thenReturn(List.of(sampleCita));
        List<Cita> result = citaService.obtenerTodas();
        assertEquals(1, result.size());
        assertEquals("Juan Perez", result.get(0).getNombrePaciente());
    }

    @Test
    void testCrearCita() {
        when(citaRepository.save(any(Cita.class))).thenReturn(sampleCita);
        Cita result = citaService.crearCita(sampleCita);
        assertNotNull(result);
        assertEquals("12345678-9", result.getRutPaciente());
    }

    @Test
    void testConfirmarCita() {
        when(citaRepository.findById(1L)).thenReturn(Optional.of(sampleCita));
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cita result = citaService.confirmarCita(1L);
        assertEquals(EstadoCita.CONFIRMADA, result.getEstado());
    }

    @Test
    void testCancelarCita() {
        when(citaRepository.findById(1L)).thenReturn(Optional.of(sampleCita));
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cita result = citaService.cancelarCita(1L);
        assertEquals(EstadoCita.CANCELADA, result.getEstado());
    }
}
