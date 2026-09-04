package cl.duoc.telemedicina.notificaciones.service;

import cl.duoc.telemedicina.notificaciones.entity.Notificacion;
import cl.duoc.telemedicina.notificaciones.entity.Notificacion.EstadoEnvio;
import cl.duoc.telemedicina.notificaciones.entity.Notificacion.TipoNotificacion;
import cl.duoc.telemedicina.notificaciones.repository.NotificacionRepository;
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

class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionService notificacionService;

    private Notificacion sampleNotificacion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleNotificacion = Notificacion.builder()
                .id(1L)
                .citaId(100L)
                .rutPaciente("12345678-9")
                .tipo(TipoNotificacion.SMS)
                .mensaje("Recordatorio Cita Medica")
                .estado(EstadoEnvio.ENVIADO)
                .confirmacionLectura(false)
                .build();
    }

    @Test
    void testEnviarRecordatorio() {
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notificacion result = notificacionService.enviarRecordatorio(100L, "12345678-9", TipoNotificacion.WHATSAPP, "Hola");
        assertNotNull(result);
        assertEquals(TipoNotificacion.WHATSAPP, result.getTipo());
        assertEquals(EstadoEnvio.ENVIADO, result.getEstado());
    }

    @Test
    void testMarcarConfirmado() {
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(sampleNotificacion));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notificacion result = notificacionService.marcarConfirmado(1L);
        assertTrue(result.getConfirmacionLectura());
    }
}
