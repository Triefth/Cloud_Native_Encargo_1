package cl.duoc.telemedicina.usuarios.service;

import cl.duoc.telemedicina.usuarios.entity.Medico;
import cl.duoc.telemedicina.usuarios.entity.Paciente;
import cl.duoc.telemedicina.usuarios.repository.MedicoRepository;
import cl.duoc.telemedicina.usuarios.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Paciente samplePaciente;
    private Medico sampleMedico;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        samplePaciente = Paciente.builder()
                .id(1L)
                .rut("12345678-9")
                .nombres("Juan")
                .apellidoPaterno("Perez")
                .build();

        sampleMedico = Medico.builder()
                .id(1L)
                .rut("98765432-1")
                .nombreCompleto("Dr. Soto")
                .especialidad("Cardiologia")
                .build();
    }

    @Test
    void testObtenerTodosPacientes() {
        when(pacienteRepository.findAll()).thenReturn(List.of(samplePaciente));
        List<Paciente> result = usuarioService.obtenerTodosPacientes();
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getNombres());
    }

    @Test
    void testObtenerTodosMedicos() {
        when(medicoRepository.findAll()).thenReturn(List.of(sampleMedico));
        List<Medico> result = usuarioService.obtenerTodosMedicos();
        assertEquals(1, result.size());
        assertEquals("Dr. Soto", result.get(0).getNombreCompleto());
    }
}
