package cl.duoc.telemedicina.reportes;

import cl.duoc.telemedicina.reportes.entity.ReporteOperativo;
import cl.duoc.telemedicina.reportes.entity.ReporteOperativo.Severidad;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportesApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testResumenOperativoYRegistroEvento() throws Exception {
        mockMvc.perform(get("/api/reportes/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEventosRegistrados").exists())
                .andExpect(jsonPath("$.latenciaPromedioMs").exists())
                .andExpect(jsonPath("$.estadoPlataforma").exists());

        ReporteOperativo nuevoEvento = ReporteOperativo.builder()
                .moduloServicio("citas-service")
                .tipoEvento("LATENCIA_ELE VADA")
                .descripcion("Respuesta lenta en consulta de disponibilidad")
                .latenciaMs(250L)
                .severidad(Severidad.WARNING)
                .build();

        mockMvc.perform(post("/api/reportes/evento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoEvento)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.moduloServicio").value("citas-service"));
    }
}
