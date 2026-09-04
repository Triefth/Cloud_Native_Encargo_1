package cl.duoc.telemedicina.fichas.controller;

import cl.duoc.telemedicina.fichas.entity.FichaMedica;
import cl.duoc.telemedicina.fichas.service.FichaService;
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

class FichaControllerTest {

    @Mock
    private FichaService fichaService;

    @InjectMocks
    private FichaController fichaController;

    private FichaMedica sampleFicha;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleFicha = FichaMedica.builder()
                .id(1L)
                .rutPaciente("12345678-9")
                .nombreCompleto("Maria Gonzales")
                .build();
    }

    @Test
    void testObtenerPorRutExistente() {
        when(fichaService.obtenerPorRut("12345678-9")).thenReturn(Optional.of(sampleFicha));
        ResponseEntity<?> response = fichaController.obtenerPorRut("12345678-9");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testObtenerPorRutNoExistente() {
        when(fichaService.obtenerPorRut("00000000-0")).thenReturn(Optional.empty());
        ResponseEntity<?> response = fichaController.obtenerPorRut("00000000-0");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
