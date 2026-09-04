package cl.duoc.telemedicina.usuarios.controller;

import cl.duoc.telemedicina.usuarios.entity.Medico;
import cl.duoc.telemedicina.usuarios.entity.Paciente;
import cl.duoc.telemedicina.usuarios.service.UsuarioService;
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

class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private Paciente samplePaciente;
    private Medico sampleMedico;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        samplePaciente = Paciente.builder().id(1L).rut("12345678-9").nombres("Juan").build();
        sampleMedico = Medico.builder().id(1L).rut("98765432-1").nombreCompleto("Dr. Soto").build();
    }

    @Test
    void testObtenerPacientes() {
        when(usuarioService.obtenerTodosPacientes()).thenReturn(List.of(samplePaciente));
        ResponseEntity<List<Paciente>> response = usuarioController.obtenerPacientes();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testObtenerMedicos() {
        when(usuarioService.obtenerTodosMedicos()).thenReturn(List.of(sampleMedico));
        ResponseEntity<List<Medico>> response = usuarioController.obtenerMedicos();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}
