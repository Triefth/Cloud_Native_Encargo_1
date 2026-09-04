package cl.duoc.telemedicina.fichas.service;

import cl.duoc.telemedicina.fichas.entity.FichaMedica;
import cl.duoc.telemedicina.fichas.repository.FichaRepository;
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

class FichaServiceTest {

    @Mock
    private FichaRepository fichaRepository;

    @InjectMocks
    private FichaService fichaService;

    private FichaMedica sampleFicha;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleFicha = FichaMedica.builder()
                .id(1L)
                .rutPaciente("12345678-9")
                .nombreCompleto("Maria Gonzales")
                .clinicaOrigen("Clínica Rural San Pedro")
                .build();
    }

    @Test
    void testObtenerTodas() {
        when(fichaRepository.findAll()).thenReturn(List.of(sampleFicha));
        List<FichaMedica> result = fichaService.obtenerTodas();
        assertEquals(1, result.size());
        assertEquals("Maria Gonzales", result.get(0).getNombreCompleto());
    }

    @Test
    void testRegistrarAtencionRemota() {
        when(fichaRepository.findByRutPaciente("12345678-9")).thenReturn(Optional.of(sampleFicha));
        when(fichaRepository.save(any(FichaMedica.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FichaMedica result = fichaService.registrarAtencionRemota("12345678-9", 100L, "Dr. Soto", "Cardiología", "Paciente controlado");
        assertNotNull(result);
        assertEquals(1, result.getAtencionesRemotas().size());
        assertEquals("Dr. Soto", result.getAtencionesRemotas().get(0).getNombreMedico());
    }
}
