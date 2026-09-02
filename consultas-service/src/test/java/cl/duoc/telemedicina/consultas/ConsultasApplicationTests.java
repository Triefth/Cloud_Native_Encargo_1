package cl.duoc.telemedicina.consultas;

import cl.duoc.telemedicina.consultas.entity.Consulta;
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
class ConsultasApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testIniciarYFinalizarConsulta() throws Exception {
        String response = mockMvc.perform(post("/api/consultas/iniciar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "citaId", 101,
                        "rutPaciente", "18.452.123-9",
                        "rutMedico", "12.345.678-K"
                ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.roomName").exists())
                .andExpect(jsonPath("$.cpaasJoinUrl").exists())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"))
                .andReturn().getResponse().getContentAsString();

        Consulta consultaStarted = objectMapper.readValue(response, Consulta.class);
        Long consultaId = consultaStarted.getId();

        mockMvc.perform(put("/api/consultas/" + consultaId + "/finalizar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "diagnosticoPreliminar", "Hipertensión arterial leve",
                        "indicacionesMedicas", "Tomar Enalapril 10mg diario",
                        "duracionMinutos", 20
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADA"))
                .andExpect(jsonPath("$.diagnosticoPreliminar").value("Hipertensión arterial leve"))
                .andExpect(jsonPath("$.duracionMinutos").value(20));
    }
}
