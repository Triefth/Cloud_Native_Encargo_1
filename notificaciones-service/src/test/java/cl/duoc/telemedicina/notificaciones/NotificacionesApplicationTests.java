package cl.duoc.telemedicina.notificaciones;

import cl.duoc.telemedicina.notificaciones.entity.Notificacion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificacionesApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testEnviarYConfirmarNotificacion() throws Exception {
        String response = mockMvc.perform(post("/api/notificaciones/recordatorio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "citaId", 1,
                        "rutPaciente", "18.452.123-9",
                        "tipo", "WHATSAPP",
                        "mensaje", "Recordatorio de cita médica en 24 horas"
                ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("ENVIADO"))
                .andExpect(jsonPath("$.confirmacionLectura").value(false))
                .andReturn().getResponse().getContentAsString();

        Notificacion notif = objectMapper.readValue(response, Notificacion.class);

        mockMvc.perform(put("/api/notificaciones/" + notif.getId() + "/lectura"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmacionLectura").value(true));
    }
}
