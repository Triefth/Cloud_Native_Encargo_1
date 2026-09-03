package cl.duoc.telemedicina.fichas;

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
class FichasApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testObtenerYRegistrarAtencionFicha() throws Exception {
        String rut = "18.452.123-9";

        mockMvc.perform(get("/api/fichas/paciente/" + rut))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rutPaciente").value(rut))
                .andExpect(jsonPath("$.nombreCompleto").exists());

        mockMvc.perform(post("/api/fichas/paciente/" + rut + "/atencion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "consultaId", 201,
                        "medico", "Dr. Roberto Silva",
                        "especialidad", "Cardiología",
                        "resumen", "Control cardiológico preventivo OK"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atencionesRemotas").isArray());
    }
}
