package cl.duoc.telemedicina.clinicas;

import cl.duoc.telemedicina.clinicas.entity.ConfiguracionEhr;
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
class ClinicasApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testCatalogoyConfiguracionClinicas() throws Exception {
        mockMvc.perform(get("/api/clinicas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));

        ConfiguracionEhr nuevaConfig = ConfiguracionEhr.builder()
                .nombreSoftwareEhr("EHR Rural v2")
                .apiEndpointUrl("https://api.sanpedro.cl/v2/ehr")
                .apiKey("NEW_SECRET_API_KEY_999")
                .tipoProtocolo("REST_JSON")
                .sincronizacionAutomatica(true)
                .build();

        mockMvc.perform(put("/api/clinicas/1/configuracion-ehr")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configuracionEhr.nombreSoftwareEhr").value("EHR Rural v2"))
                .andExpect(jsonPath("$.configuracionEhr.apiKey").value("NEW_SECRET_API_KEY_999"));
    }
}
