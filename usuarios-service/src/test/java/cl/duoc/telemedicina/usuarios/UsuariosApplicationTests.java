package cl.duoc.telemedicina.usuarios;

import cl.duoc.telemedicina.usuarios.entity.Paciente;
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
class UsuariosApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testCatalogosUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/usuarios/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));

        mockMvc.perform(get("/api/usuarios/medicos/especialidad/Cardiología"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void testCrearPaciente() throws Exception {
        Paciente nuevo = Paciente.builder()
                .rut("20.111.222-3")
                .nombres("Ana Maria")
                .apellidoPaterno("Fuentes")
                .apellidoMaterno("Soto")
                .email("ana.fuentes@correo.cl")
                .telefonoContacto("+56911223344")
                .direccionRural("Camino Rural Km 12, Huara")
                .clinicaAsignada("Clínica Rural de Huara")
                .build();

        mockMvc.perform(post("/api/usuarios/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.rut").value("20.111.222-3"));
    }
}
