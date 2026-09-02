package cl.duoc.telemedicina.citas;

import cl.duoc.telemedicina.citas.entity.Cita;
import cl.duoc.telemedicina.citas.entity.Cita.EstadoCita;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CitasApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testObtenerCitas() throws Exception {
        mockMvc.perform(get("/api/citas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void testCrearYModificarCita() throws Exception {
        Cita nuevaCita = Cita.builder()
                .rutPaciente("19.876.543-2")
                .nombrePaciente("Carlos Mendoza")
                .rutMedico("11.222.333-4")
                .nombreMedico("Dr. Andrés Soto")
                .especialidad("Pediatría")
                .fechaHora(LocalDateTime.now().plusDays(5))
                .motivoConsulta("Fiebre persistente")
                .clinicaRural("Clínica Rural Putre")
                .build();

        String json = objectMapper.writeValueAsString(nuevaCita);

        String response = mockMvc.perform(post("/api/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombrePaciente").value("Carlos Mendoza"))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andReturn().getResponse().getContentAsString();

        Cita citaCreada = objectMapper.readValue(response, Cita.class);
        Long id = citaCreada.getId();

        // Confirmar cita
        mockMvc.perform(put("/api/citas/" + id + "/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        // Reprogramar cita
        LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(7).withHour(14).withMinute(0);
        mockMvc.perform(put("/api/citas/" + id + "/reprogramar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("nuevaFechaHora", nuevaFecha.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REPROGRAMADA"));

        // Cancelar cita
        mockMvc.perform(put("/api/citas/" + id + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }
}
