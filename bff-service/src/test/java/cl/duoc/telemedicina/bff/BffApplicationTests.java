package cl.duoc.telemedicina.bff;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BffApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testDevTokenEndpoint() throws Exception {
        mockMvc.perform(get("/api/bff/auth/dev-token")
                .param("user", "test.medico@rural.cl")
                .param("role", "MEDICO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.user").value("test.medico@rural.cl"))
                .andExpect(jsonPath("$.role").value("MEDICO"));
    }

    @Test
    void testValidateTokenEndpoint() throws Exception {
        MvcResult tokenResult = mockMvc.perform(get("/api/bff/auth/dev-token")
                .param("user", "test.paciente@rural.cl")
                .param("role", "PACIENTE"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = tokenResult.getResponse().getContentAsString();
        Map<?, ?> map = objectMapper.readValue(responseBody, Map.class);
        String token = (String) map.get("token");

        mockMvc.perform(post("/api/bff/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("token", token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("test.paciente@rural.cl"));
    }

    @Test
    void testUnauthorizedRequest() throws Exception {
        mockMvc.perform(get("/api/bff/citas"))
                .andExpect(status().isUnauthorized());
    }
}
