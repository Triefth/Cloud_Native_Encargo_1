package cl.duoc.telemedicina.notificaciones.controller;

import cl.duoc.telemedicina.notificaciones.entity.Notificacion;
import cl.duoc.telemedicina.notificaciones.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class NotificacionControllerTest {

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private NotificacionController notificacionController;

    private Notificacion sampleNotificacion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleNotificacion = Notificacion.builder()
                .id(1L)
                .rutPaciente("12345678-9")
                .mensaje("Recordatorio")
                .build();
    }

    @Test
    void testObtenerTodas() {
        when(notificacionService.obtenerTodas()).thenReturn(List.of(sampleNotificacion));
        ResponseEntity<List<Notificacion>> response = notificacionController.obtenerTodas();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }
}
